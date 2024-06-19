package net.cserny.move;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Tracer.SpanInScope;
import lombok.extern.slf4j.Slf4j;
import net.cserny.download.DownloadedMedia;
import net.cserny.download.DownloadedMediaRepository;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.rename.*;
import net.cserny.rename.NameNormalizer.NameYear;
import net.cserny.search.MediaFileGroup;
import net.cserny.search.MediaSearchService;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.lang.String.format;

// TODO: add tests
@Slf4j
@Service
@ConditionalOnProperty(prefix = "automove", name = "enabled", havingValue = "true")
public class AutoMoveMediaService {

    @Autowired
    private DownloadedMediaRepository downloadedMediaRepository;

    @Autowired
    private AutoMoveMediaRepository autoMoveMediaRepository;

    @Autowired
    private MediaSearchService searchService;

    @Autowired
    private MediaRenameService renameService;

    @Autowired
    private MediaMoveService moveService;

    @Autowired
    private NameNormalizer normalizer;

    @Autowired
    private LocalFileService fileService;

    @Autowired
    private FilesystemProperties filesystemProperties;

    @Autowired
    private Tracer tracer;

    @Autowired
    private AutoMoveProperties properties;

    private final ExecutorService threadpool;

    public AutoMoveMediaService() {
        this.threadpool = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Scheduled(initialDelayString = "${automove.initial-delay-ms}", fixedDelayString = "${automove.cron-ms}")
    public void autoMoveMedia() throws InterruptedException {
        log.info("Checking download cache to automatically move media files");

        List<DownloadedMedia> medias = downloadedMediaRepository.retrieveForAutoMove(false, properties.getLimit());
        if (medias.isEmpty()) {
            log.info("No media found to auto move");
            return;
        }

        log.info("Trying to automatically move {} media file", medias.size());

        List<Callable<Void>> mediaProcesses = new ArrayList<>();
        for (DownloadedMedia media : medias) {
            mediaProcesses.add(createCallable(media));
        }

        threadpool.invokeAll(mediaProcesses);
        updateDownloadedMedia(medias);

        log.info("Finished checking download cache to automatically move media files");
    }

    private Callable<Void> createCallable(DownloadedMedia media) {
        Span nextSpan = this.tracer.nextSpan();

        return () -> {
            try (SpanInScope ignored = this.tracer.withSpan(nextSpan.start())) {
                LocalPath path = fileService.toLocalPath(filesystemProperties.getDownloadsPath(), media.getFileName());
                if (!fileService.exists(path)) {
                    log.info("Path doesn't exist: {}, skipping...", path);
                    return null;
                }

                List<MediaFileGroup> groups = searchService.generateMediaFileGroups(List.of(path.path()));
                if (groups.isEmpty()) {
                    log.info("No media groups generated, skipping...");
                    return null;
                }

                MediaFileGroup group = groups.getFirst();
                NameYear nameYear = normalizer.normalize(group.name());

                Map<Triple<MediaDescription, MediaFileType, MediaRenameOrigin>, Integer> sortedMap = processOptions(group, nameYear);
                if (sortedMap.isEmpty()) {
                    log.info("No options were similar enough to automove media");
                    return null;
                }

                Triple<MediaDescription, MediaFileType, MediaRenameOrigin> option = sortedMap.keySet().stream().findFirst().get();
                log.info("Using first option to move media {}", media);

                String movedName = moveMedia(option, group);
                saveAutoMove(media, movedName, sortedMap.get(option), option);
            } catch (Exception e) {
                log.error("Error occurred in virtual thread", e);
            } finally {
                nextSpan.end();
            }

            return null;
        };
    }

    private Map<Triple<MediaDescription, MediaFileType, MediaRenameOrigin>, Integer> processOptions(MediaFileGroup group, NameYear nameYear) {
        Map<Triple<MediaDescription, MediaFileType, MediaRenameOrigin>, Integer> optionsMap = new HashMap<>();

        log.info("Producing movie options");
        RenamedMediaOptions movieOptions = renameService.produceNames(group.name(), MediaFileType.MOVIE);
        movieOptions.mediaDescriptions().forEach(mediaDescription -> {
            updateOptionsMap(optionsMap, mediaDescription, MediaFileType.MOVIE, movieOptions.origin(), nameYear.name());
        });

        log.info("Producing TV options");
        RenamedMediaOptions tvOptions = renameService.produceNames(group.name(), MediaFileType.TV);
        tvOptions.mediaDescriptions().forEach(mediaDescription -> {
            updateOptionsMap(optionsMap, mediaDescription, MediaFileType.TV, tvOptions.origin(), nameYear.name());
        });

        log.info("Options parsed: {}", optionsMap);

        Map<Triple<MediaDescription, MediaFileType, MediaRenameOrigin>, Integer> sortedMap = optionsMap.entrySet().stream()
                .filter(entry -> entry.getValue() >= properties.getSimilarityAccepted())
                .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        log.info("Sorted options map by similarity: {}", sortedMap);
        return sortedMap;
    }

    private String moveMedia(Triple<MediaDescription, MediaFileType, MediaRenameOrigin> option, MediaFileGroup group) {
        MediaDescription desc = option.getLeft();
        String movedName = desc.title() + (desc.date() != null && !desc.date().isEmpty() ? format(" (%s)", desc.date()) : "");
        MediaFileGroup resultGroup = new MediaFileGroup(group.path(), movedName, group.videos());
        moveService.moveMedia(resultGroup, option.getMiddle());
        return movedName;
    }

    private void updateDownloadedMedia(List<DownloadedMedia> medias) {
        medias.forEach(m -> m.setTriedAutoMove(true));
        downloadedMediaRepository.saveAll(medias);
    }

    private void saveAutoMove(DownloadedMedia media, String movedName, Integer similarityPercent,
                              Triple<MediaDescription, MediaFileType, MediaRenameOrigin> option) {
        AutoMoveMedia autoMoveMedia = new AutoMoveMedia();
        autoMoveMedia.setFileName(media.getFileName());
        autoMoveMedia.setMovedName(movedName);
        autoMoveMedia.setMoveDate(Instant.now(Clock.systemUTC()));
        autoMoveMedia.setSimilarityPercent(similarityPercent);
        autoMoveMedia.setOrigin(option.getRight());
        autoMoveMedia.setType(option.getMiddle());
        autoMoveMediaRepository.save(autoMoveMedia);
    }

    private void updateOptionsMap(Map<Triple<MediaDescription, MediaFileType, MediaRenameOrigin>, Integer> optionsMap,
                                  MediaDescription description, MediaFileType type, MediaRenameOrigin origin, String compare) {
        String source = description.title();
        int distance = SimilarityService.getDistance(source, compare);
        int percent = SimilarityService.getSimilarityPercent(distance, compare.length());
        optionsMap.put(Triple.of(description, type, origin), percent);
    }
}
