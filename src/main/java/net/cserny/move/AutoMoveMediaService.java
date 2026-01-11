package net.cserny.move;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.cserny.support.Features;
import net.cserny.download.internal.DownloadedMediaRepository;
import net.cserny.download.DownloadedMedia;
import net.cserny.generated.*;
import net.cserny.rename.*;
import net.cserny.rename.NameNormalizer.NameYear;
import net.cserny.search.MediaSearchService;
import net.cserny.support.UtilityProvider;
import org.jspecify.annotations.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.togglz.core.manager.FeatureManager;

import java.io.File;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static net.cserny.support.UtilityProvider.toOneLineString;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoMoveMediaService {

    private static final String separator = File.separator;
    private static final String quotedSeparator = Pattern.quote(separator);

    private final FeatureManager featureManager;
    private final AutoMoveProperties properties;
    private final ExecutorService executorService;

    private final DownloadedMediaRepository downloadedMediaRepository;
    private final AutoMoveMediaRepository autoMoveMediaRepository;

    private final NameNormalizer normalizer;
    private final MediaSearchService searchService;
    private final MediaRenameService renameService;
    private final MediaMoveService moveService;

    @SneakyThrows
    @Scheduled(cron = "${automove.cron}")
    public void autoMoveMedia() {
        if (!featureManager.isActive(Features.AUTOMOVE)) {
            return;
        }

        log.info("Checking download cache to automatically move media files");

        List<DownloadedMedia> medias = downloadedMediaRepository.findForAutoMove(properties.getLimit());
        if (medias.isEmpty()) {
            log.info("No media found to auto move");
            return;
        }

        log.info("Trying to automatically move {} media file", medias.size());

        List<List<DownloadedMedia>> groupedMedias = new ArrayList<>(medias.stream().collect(Collectors.groupingBy(customMediaFileName())).values());
        List<Future<Void>> results = executorService.invokeAll(groupedMedias.stream().map(mediaProcessingMapping()).toList());

        for (Future<Void> result : results) {
            try {
                result.get();
            } catch (RuntimeException e) {
                log.error("Automatic move media failed", e);
            }
        }

        updateDownloadedMedia(medias);

        log.info("Finished checking download cache to automatically move media files");
    }

    private @NonNull Function<List<DownloadedMedia>, Callable<Void>> mediaProcessingMapping() {
        return mediaList -> () -> {
            processMediaList(mediaList);
            return null;
        };
    }

    private static @NonNull Function<DownloadedMedia, String> customMediaFileName() {
        return media -> {
            String fileName = media.getFileName();
            if (fileName.contains(separator)) {
                return fileName.split(quotedSeparator)[0];
            }
            return fileName;
        };
    }

    private void processMediaList(List<DownloadedMedia> mediaList) {
        List<String> relativeMediaPaths = mediaList.stream().map(DownloadedMedia::getFileName).toList();

        List<MediaFileGroup> groups = searchService.generateMediaFileGroupsFromDownloads(relativeMediaPaths);
        if (groups.isEmpty()) {
            log.info("No media groups generated, skipping...");
            return;
        }

        if (groups.size() > 1) {
            throw new IllegalStateException("Multiple media groups generated for the same grouped downloaded media: " + relativeMediaPaths);
        }

        MediaFileGroup group = groups.getFirst();
        NameYear nameYear = normalizer.normalize(group.getName());

        Optional<AutoMoveOption> autoMoveOptional = processOptions(group, nameYear);
        if (autoMoveOptional.isEmpty()) {
            log.info("No options were similar enough to automove media");
            return;
        }

        AutoMoveOption option = autoMoveOptional.get();
        log.info("Using first option to move media {}", toOneLineString(option));

        String movedName = moveMedia(option, group);
        saveAutoMove(mediaList, movedName, option);
    }

    @SneakyThrows
    private Optional<AutoMoveOption> processOptions(MediaFileGroup group, NameYear nameYear) {
        List<Future<List<AutoMoveOption>>> listOfAllOptions = executorService.invokeAll(List.of(
                () -> produceOptions(group.getName(), MediaFileType.MOVIE, nameYear.name()),
                () -> produceOptions(group.getName(), MediaFileType.TV, nameYear.name())
        ));
        List<AutoMoveOption> allOptions = listOfAllOptions.stream().map(UtilityProvider::getUncheckedThrowing).flatMap(List::stream).toList();

        log.info("Options parsed: {}", toOneLineString(allOptions));

        MediaTypeComparatorProvider compProvider = new MediaTypeComparatorProvider(group, nameYear);
        List<AutoMoveOption> sortedOptions = allOptions.stream()
                .filter(o -> o.origin != MediaRenameOrigin.NAME)
                .filter(o -> o.similarity() >= properties.getSimilarityAccepted())
                .sorted(comparing(AutoMoveOption::similarity, reverseOrder())
                        .thenComparing(compProvider.provide()))
                .collect(Collectors.toCollection(LinkedList::new));

        log.info("Sorted options map by similarity: {}", toOneLineString(sortedOptions));
        return sortedOptions.stream().findFirst();
    }

    private String moveMedia(AutoMoveOption option, MediaFileGroup group) {
        MediaDescriptionData desc = option.desc();
        String movedName = desc.getTitle() + (desc.getDate() != null && !desc.getDate().isEmpty() ? format(" (%s)", desc.getDate()) : "");
        MediaFileGroup resultGroup = new MediaFileGroup().path(group.getPath())
                .name(movedName)
                .noParent(group.getNoParent())
                .videos(group.getVideos());
        moveService.moveMedia(resultGroup, option.type());
        return movedName;
    }

    private void updateDownloadedMedia(List<DownloadedMedia> medias) {
        medias.forEach(m -> m.setTriedAutoMove(true));
        downloadedMediaRepository.saveAll(medias);
    }

    private void saveAutoMove(List<DownloadedMedia> medias, String movedName, AutoMoveOption option) {
        List<AutoMoveMedia> automovedMedia = medias.stream().map(m -> {
            AutoMoveMedia autoMoveMedia = new AutoMoveMedia();
            autoMoveMedia.setFileName(m.getFileName());
            autoMoveMedia.setMovedName(movedName);
            autoMoveMedia.setMoveDate(Instant.now(Clock.systemUTC()));
            autoMoveMedia.setSimilarityPercent(option.similarity());
            autoMoveMedia.setOrigin(option.origin().getValue());
            autoMoveMedia.setType(option.type().getValue());
            return autoMoveMedia;
        }).toList();

        autoMoveMediaRepository.saveAll(automovedMedia);
    }

    private List<AutoMoveOption> produceOptions(String groupName, MediaFileType type, String compare) {
        try {
            log.info("Producing {} options", type.toString());
            RenamedMediaOptions options = renameService.produceNames(groupName, type);
            return options.getMediaDescriptions().stream()
                    .map(description -> {
                        String source = description.getTitle();
                        int distance = SimilarityService.getDistance(source, compare);
                        int percent = SimilarityService.getSimilarityPercent(distance, compare.length());
                        return new AutoMoveOption(description, type, options.getOrigin(), percent);
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Error occurred in virtual thread: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    record AutoMoveOption(MediaDescriptionData desc, MediaFileType type, MediaRenameOrigin origin, int similarity) { }
}
