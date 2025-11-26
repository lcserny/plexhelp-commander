package net.cserny.move;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.Features;
import net.cserny.VirtualExecutor;
import net.cserny.download.internal.DownloadedMediaRepository;
import net.cserny.download.DownloadedMedia;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.generated.*;
import net.cserny.rename.*;
import net.cserny.rename.NameNormalizer.NameYear;
import net.cserny.search.MediaIdentificationService;
import net.cserny.search.MediaSearchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.togglz.core.manager.FeatureManager;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static net.cserny.CommanderApplication.toOneLineString;

// TODO this needs cleanup, too many deps

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoMoveMediaService {

    private final FeatureManager featureManager;

    private final DownloadedMediaRepository downloadedMediaRepository;
    private final AutoMoveMediaRepository autoMoveMediaRepository;
    private final MediaSearchService searchService;
    private final MediaRenameService renameService;
    private final MediaMoveService moveService;
    private final NameNormalizer normalizer;
    private final LocalFileService fileService;
    private final FilesystemProperties filesystemProperties;
    private final AutoMoveProperties properties;
    private final VirtualExecutor threadpool;
    private final MediaIdentificationService identificationService;

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
        threadpool.executeWithNewSpans(medias.stream().<Callable<Void>>map(m -> () -> {
            processMedia(m);
            return null;
        }));

        updateDownloadedMedia(medias);

        log.info("Finished checking download cache to automatically move media files");
    }

    private void processMedia(DownloadedMedia media) {
        try {
            LocalPath path = fileService.toLocalPath(filesystemProperties.getDownloadsPath(), media.getFileName());
            if (!fileService.exists(path)) {
                log.info("Path doesn't exist: {}, skipping...", path);
                return;
            }

            if (!identificationService.isMedia(path)) {
                log.info("Path is not a valid media file: {}, skipping...", path);
                return;
            }

            List<MediaFileGroup> groups = searchService.generateMediaFileGroups(List.of(path));
            if (groups.isEmpty()) {
                log.info("No media groups generated, skipping...");
                return;
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
            saveAutoMove(media, movedName, option);
        } catch (Exception e) {
            log.error("Error occurred in virtual thread: {}", e.getMessage());
        }
    }

    private Optional<AutoMoveOption> processOptions(MediaFileGroup group, NameYear nameYear) {
        List<List<AutoMoveOption>> listOfAllOptions = this.threadpool.executeWithCurrentSpan(Stream.of(
                () -> produceOptions(group.getName(), MediaFileType.MOVIE, nameYear.name()),
                () -> produceOptions(group.getName(), MediaFileType.TV, nameYear.name())
        ));
        List<AutoMoveOption> allOptions = listOfAllOptions.stream().flatMap(List::stream).toList();

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

    private void saveAutoMove(DownloadedMedia media, String movedName, AutoMoveOption option) {
        AutoMoveMedia autoMoveMedia = new AutoMoveMedia();
        autoMoveMedia.setFileName(media.getFileName());
        autoMoveMedia.setMovedName(movedName);
        autoMoveMedia.setMoveDate(Instant.now(Clock.systemUTC()));
        autoMoveMedia.setSimilarityPercent(option.similarity());
        autoMoveMedia.setOrigin(option.origin().getValue());
        autoMoveMedia.setType(option.type().getValue());
        autoMoveMediaRepository.save(autoMoveMedia);
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
