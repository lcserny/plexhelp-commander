package net.cserny.move;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaMoveError;
import net.cserny.search.MediaIdentificationService;
import net.cserny.search.SearchProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.cserny.CommanderApplication.toOneLineString;

@Service
@Slf4j
public class MediaMoveService {

    private static final String MOVIE_EXISTS = "Movie already exists";

    private List<String> importantFolders = new ArrayList<>();

    @Autowired
    LocalFileService fileService;

    @Autowired
    SubtitleMover subtitleMover;

    @Autowired
    SearchProperties searchConfig;

    @Autowired
    FilesystemProperties filesystemConfig;

    @Autowired
    MoveProperties moveConfig;

    @Autowired
    MediaIdentificationService identificationService;

    @Autowired
    VideosGrouper videosGrouper;

    @PostConstruct
    public void init() {
        this.importantFolders = List.of(
            filesystemConfig.getDownloadsPath(),
            filesystemConfig.getMoviesPath(),
            filesystemConfig.getTvPath()
        );
        log.info("Important folders: {}", toOneLineString(this.importantFolders));
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
            DestinationProducer dest = new DestinationProducer(fileGroup, type, videoNameOnly);
            LocalPath destPath = fileService.toLocalPath(destRoot, dest.getDestSegments());

            try {
                if (fileService.exists(destPath)) {
                    throw new IOException("Media already exists: " + destPath);
                }

                log.info("Moving video {} to {}", srcPath, destPath);
                fileService.move(srcPath, destPath);
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
