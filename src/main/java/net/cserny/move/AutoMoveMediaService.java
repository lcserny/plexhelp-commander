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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "automove", name = "enabled", havingValue = "true")
// enable this as a feature: https://www.togglz.org/documentation/spring-boot-starter
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
            Span nextSpan = this.tracer.nextSpan();
            mediaProcesses.add(() -> {
                try (SpanInScope ignored = this.tracer.withSpan(nextSpan.start())) {
                    processMedia(media);
                } finally {
                    nextSpan.end();
                }
                return null;
            });
        }

        threadpool.invokeAll(mediaProcesses);
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

            List<MediaFileGroup> groups = searchService.generateMediaFileGroups(List.of(path.path()));
            if (groups.isEmpty()) {
                log.info("No media groups generated, skipping...");
                return;
            }

            MediaFileGroup group = groups.getFirst();
            NameYear nameYear = normalizer.normalize(group.name());

            Optional<AutoMoveOption> autoMoveOptional = processOptions(group, nameYear);
            if (autoMoveOptional.isEmpty()) {
                log.info("No options were similar enough to automove media");
                return;
            }

            AutoMoveOption option = autoMoveOptional.get();
            log.info("Using first option to move media {}", option);

            String movedName = moveMedia(option, group);
            saveAutoMove(media, movedName, option);
        } catch (Exception e) {
            log.error("Error occurred in virtual thread", e);
        }
    }

    private Optional<AutoMoveOption> processOptions(MediaFileGroup group, NameYear nameYear) throws InterruptedException, ExecutionException {
        Span currentSpan = this.tracer.currentSpan();
        Future<List<AutoMoveOption>> movieAutoMoveOptionsFuture = threadpool.submit(() -> {
            try (SpanInScope ignored = this.tracer.withSpan(currentSpan)) {
                return produceOptions(group.name(), MediaFileType.MOVIE, nameYear.name());
            }
        });
        Future<List<AutoMoveOption>> tvAutoMoveOptionsFuture = threadpool.submit(() -> {
            try (SpanInScope ignored = this.tracer.withSpan(currentSpan)) {
                return produceOptions(group.name(), MediaFileType.TV, nameYear.name());
            }
        });

        List<AutoMoveOption> allOptions = Stream.concat(movieAutoMoveOptionsFuture.get().stream(), tvAutoMoveOptionsFuture.get().stream()).toList();

        log.info("Options parsed: {}", allOptions);

        List<AutoMoveOption> sortedOptions = allOptions.stream()
                .filter(o -> o.origin != MediaRenameOrigin.NAME)
                .filter(o -> o.similarity() >= properties.getSimilarityAccepted())
                .sorted(comparing(AutoMoveOption::similarity, reverseOrder())
                        .thenComparing(movieYearBiasedMediaComparator(nameYear)))
                .collect(Collectors.toCollection(LinkedList::new));

        log.info("Sorted options map by similarity: {}", sortedOptions);
        return sortedOptions.stream().findFirst();
    }

    // if year was present in initial name, then its most probably a movie
    private Comparator<AutoMoveOption> movieYearBiasedMediaComparator(NameYear nameYear) {
        return (o1, o2) -> {
            if (nameYear.year() != null) {
                if (o1.type() == MediaFileType.MOVIE) {
                    return o2.type() == MediaFileType.MOVIE ? 0 : -1;
                }
                if (o2.type() == MediaFileType.MOVIE) {
                    return 1;
                }
            } else {
                if (o1.type() == MediaFileType.TV) {
                    return o2.type() == MediaFileType.TV ? 0 : -1;
                }
                if (o2.type() == MediaFileType.TV) {
                    return 1;
                }
            }
            return 0;
        };
    }

    private String moveMedia(AutoMoveOption option, MediaFileGroup group) {
        MediaDescription desc = option.desc();
        String movedName = desc.title() + (desc.date() != null && !desc.date().isEmpty() ? format(" (%s)", desc.date()) : "");
        MediaFileGroup resultGroup = new MediaFileGroup(group.path(), movedName, group.videos());
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
        autoMoveMedia.setOrigin(option.origin());
        autoMoveMedia.setType(option.type());
        autoMoveMediaRepository.save(autoMoveMedia);
    }

    private List<AutoMoveOption> produceOptions(String groupName, MediaFileType type, String compare) {
        try {
            log.info("Producing {} options", type);
            RenamedMediaOptions options = renameService.produceNames(groupName, type);
            return options.mediaDescriptions().stream()
                    .map(description -> {
                        String source = description.title();
                        int distance = SimilarityService.getDistance(source, compare);
                        int percent = SimilarityService.getSimilarityPercent(distance, compare.length());
                        return new AutoMoveOption(description, type, options.origin(), percent);
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Error occurred in virtual thread", e);
            return Collections.emptyList();
        }
    }

    private record AutoMoveOption(MediaDescription desc, MediaFileType type, MediaRenameOrigin origin, int similarity) {
    }
}
