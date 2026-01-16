package net.cserny.move;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaMoveError;
import net.cserny.generated.MovedMediaData;
import net.cserny.move.MediaInfoExtractor.MediaInfo;
import net.cserny.search.MediaIdentificationService;
import net.cserny.search.SearchProperties;
import net.cserny.support.DataMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static net.cserny.support.UtilityProvider.toLoggableString;

@RequiredArgsConstructor
@Service
@Slf4j
public class MediaMoveService {

    private static final String MOVIE_EXISTS = "Movie already exists";

    private List<String> importantFolders = new ArrayList<>();

    private final LocalFileService fileService;
    private final SubtitleMover subtitleMover;
    private final SearchProperties searchConfig;
    private final FilesystemProperties filesystemConfig;
    private final MoveProperties moveConfig;
    private final MediaIdentificationService identificationService;
    private final VideosGrouper videosGrouper;
    private final MovedMediaRepository movedMediaRepository;

    @PostConstruct
    public void init() {
        this.importantFolders = Stream.of(
                        filesystemConfig.getDownloadsPath(),
                        filesystemConfig.getMoviesPath(),
                        filesystemConfig.getTvPath()
                )
                .filter(Objects::nonNull)
                .toList();

        log.info("Important folders: {}", toLoggableString(this.importantFolders));
    }

    public List<MediaMoveError> moveMedia(MediaFileGroup fileGroup, MediaFileType type) {
        List<MediaMoveError> errors = new ArrayList<>();

        if (movieExists(fileGroup.getName(), type)) {
            log.info("Movie already exists {}", fileGroup.getName());
            return List.of(new MediaMoveError().mediaPath(fileGroup.getName()).error(MOVIE_EXISTS));
        }

        String destRoot = switch (type) {
            case MOVIE -> filesystemConfig.getMoviesPath();
            case TV -> filesystemConfig.getTvPath();
        };

        GroupedVideos groupedVideos = videosGrouper.group(fileGroup, type);

        for (String video : groupedVideos.videos()) {
            LocalPath srcPath = fileService.toLocalPath(fileGroup.getPath(), video);

            String videoNameOnly = fileService.toLocalPath(video).path().getFileName().toString();
            MediaInfoExtractor extractor = new MediaInfoExtractor(fileGroup.getName(), fileGroup.getSeason(), type, videoNameOnly);
            MediaInfo mediaInfo = extractor.extractMediaInfo();
            LocalPath destPath = fileService.toLocalPath(destRoot, mediaInfo.destinationPathSegments());

            try {
                log.info("Moving video {} to {}", srcPath, destPath);
                boolean moveSuccessful = fileService.move(srcPath, destPath);

                if (!moveSuccessful) {
                    movedMediaRepository.save(MovedMedia.builder()
                            .source(srcPath.path().toString())
                            .destination(destPath.path().toString())
                            .sizeBytes(srcPath.attributes().size())
                            .mediaName(mediaInfo.baseName())
                            .date(mediaInfo.date() != null ? mediaInfo.date().atStartOfDay(ZoneOffset.UTC).toInstant() : null)
                            .season(mediaInfo.season())
                            .episode(mediaInfo.episode())
                            .mediaType(type)
                            .deleted(false)
                            .build());
                }
            } catch (IOException e) {
                log.warn("Could not move media: {}", e.getMessage());
                errors.add(new MediaMoveError().mediaPath(srcPath.path().toString()).error(e.getMessage()));
            }
        }

        LocalPath subsSrc = fileService.toLocalPath(fileGroup.getPath());
        SubsMoveOperation subsMoveOperation = new SubsMoveOperation(subsSrc, destRoot, type, fileGroup);
        errors.addAll(subtitleMover.moveSubs(subsMoveOperation));

        if (errors.isEmpty()) {
            try {
                log.info("Cleaning source media folders {}", fileGroup.getPath());
                cleanSourceMediaDir(fileGroup, groupedVideos.deletableVideos());
            } catch (IOException e) {
                log.warn("Could not clean source media folder: {}", e.getMessage());
                errors.add(new MediaMoveError().mediaPath(fileGroup.getPath()).error(e.getMessage()));
            }
        }

        return errors;
    }

    public Page<MovedMediaData> getAllMovedMedia(Pageable pageable) {
        Page<MovedMedia> foundMedia = movedMediaRepository.findAll(pageable);
        return foundMedia.map(DataMapper.INSTANCE::movedMediaToMovedMediaData);
    }

    private void cleanSourceMediaDir(MediaFileGroup mediaFileGroup, List<LocalPath> deletableVideos) throws IOException {
        LocalPath removePath = fileService.toLocalPath(mediaFileGroup.getPath());

        for (String folder : importantFolders) {
            if (mediaFileGroup.getPath().equals(folder)) {
                log.info("Clean source media dir aborted, important folder, {}", folder);
                return;
            }
        }

        for (String restrictedPath : moveConfig.getRestrictedRemovePaths()) {
            if (removePath.path().getFileName().toString().equals(restrictedPath)) {
                log.info("Clean source media dir aborted, restricted folder, {}", restrictedPath);
                return;
            }
        }

        List<LocalPath> files = fileService.walk(removePath, searchConfig.getMaxDepth());
        List<LocalPath> allVideos = files.stream()
                .filter(identificationService::isMedia)
                .filter(p -> !deletableVideos.contains(p))
                .toList();
        if (!allVideos.isEmpty()) {
            log.info("Clean source media dir aborted, there are still other media in this dir");
            return;
        }

        fileService.deleteDirectory(removePath);
    }

    private boolean movieExists(String movieName, MediaFileType type) {
        if (type == MediaFileType.MOVIE) {
            LocalPath moviePath = fileService.toLocalPath(filesystemConfig.getMoviesPath(), movieName);
            return fileService.exists(moviePath) && moviePath.attributes().isDirectory();
        }
        return false;
    }
}
