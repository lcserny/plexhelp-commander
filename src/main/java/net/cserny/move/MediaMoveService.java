package net.cserny.move;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.rename.MediaFileType;
import net.cserny.search.MediaFileGroup;
import net.cserny.search.MediaIdentificationService;
import net.cserny.search.SearchProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    VideosParser videosParser;

    @PostConstruct
    public void init() {
        this.importantFolders = List.of(
            filesystemConfig.getDownloadsPath(),
            filesystemConfig.getMoviesPath(),
            filesystemConfig.getTvPath()
        );
        log.info("Important folders: {}", this.importantFolders);
    }

    public List<MediaMoveError> moveMedia(MediaFileGroup fileGroup, MediaFileType type) {
        List<MediaMoveError> errors = new ArrayList<>();

        if (movieExists(fileGroup.name(), type)) {
            log.info("Movie already exists {}", fileGroup.name());
            return List.of(new MediaMoveError(fileGroup.name(), MOVIE_EXISTS));
        }

        String destRoot = switch (type) {
            case MOVIE -> filesystemConfig.getMoviesPath();
            case TV -> filesystemConfig.getTvPath();
        };

        ParsedVideos parsedVideos = videosParser.parse(fileGroup, type);
        List<String> videos = parsedVideos.videos();
        List<LocalPath> deletableVideos = parsedVideos.deletableVideos();

        for (String video : videos) {
            LocalPath srcPath = fileService.toLocalPath(fileGroup.path(), video);
            String videoNameOnly = fileService.toLocalPath(video).path().getFileName().toString();
            LocalPath destPath = fileService.toLocalPath(destRoot, fileGroup.name(), videoNameOnly);

            try {
                log.info("Moving video {} to {}", srcPath, destPath);
                fileService.move(srcPath, destPath);
            } catch (IOException e) {
                log.warn("Could not move media", e);
                errors.add(new MediaMoveError(srcPath.path().toString(), e.getMessage()));
            }
        }

        LocalPath subsSrc = fileService.toLocalPath(fileGroup.path());
        LocalPath subsDest = fileService.toLocalPath(destRoot, fileGroup.name());
        SubsMoveOperation subsMoveOperation = new SubsMoveOperation(subsSrc, subsDest, type);
        errors.addAll(subtitleMover.moveSubs(subsMoveOperation));

        if (errors.isEmpty()) {
            try {
                log.info("Cleaning source media folders {}", fileGroup.path());
                cleanSourceMediaDir(fileGroup, deletableVideos);
            } catch (IOException e) {
                log.warn("Could not clean source media folder", e);
                errors.add(new MediaMoveError(fileGroup.path(), e.getMessage()));
            }
        }

        return errors;
    }

    private void cleanSourceMediaDir(MediaFileGroup mediaFileGroup, List<LocalPath> deletableVideos) throws IOException {
        LocalPath removePath = fileService.toLocalPath(mediaFileGroup.path());

        for (String folder : importantFolders) {
            if (mediaFileGroup.path().equals(folder)) {
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
