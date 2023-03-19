package net.cserny.move;

import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.rename.MediaFileType;
import net.cserny.search.MediaFileGroup;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MediaMoveService {

    private static final Logger LOGGER = Logger.getLogger(MediaMoveService.class);
    private static final String MOVIE_EXISTS = "Movie already exists";

    private List<String> importantFolders = new ArrayList<>();

    @Inject
    LocalFileService fileService;

    @Inject
    SubtitleMover subtitleMover;

    @Inject
    FilesystemConfig filesystemConfig;

    @Inject
    MoveConfig moveConfig;

    @PostConstruct
    public void init() {
        this.importantFolders = List.of(
                filesystemConfig.downloadsPath(),
                filesystemConfig.moviesPath(),
                filesystemConfig.tvShowsPath());
    }

    public List<MediaMoveError> moveMedia(MediaFileGroup fileGroup, MediaFileType type) {
        List<MediaMoveError> errors = new ArrayList<>();

        if (movieExists(fileGroup.name(), type)) {
            return List.of(new MediaMoveError(fileGroup.name(), MOVIE_EXISTS));
        }

        String destRoot = switch (type) {
            case MOVIE -> filesystemConfig.moviesPath();
            case TV -> filesystemConfig.tvShowsPath();
        };

        for (String video : fileGroup.videos()) {
            LocalPath srcPath = fileService.toLocalPath(fileGroup.path(), video);
            LocalPath destPath = fileService.toLocalPath(destRoot, fileGroup.name(), video);

            try {
                fileService.move(srcPath, destPath);
            } catch (IOException e) {
                LOGGER.warn("Could not move media", e);
                errors.add(new MediaMoveError(srcPath.path().toString(), e.getMessage()));
            }
        }

        LocalPath subsSrc = fileService.toLocalPath(fileGroup.path());
        LocalPath subsDest = fileService.toLocalPath(destRoot, fileGroup.name());
        SubsMoveOperation subsMoveOperation = new SubsMoveOperation(subsSrc, subsDest, type);
        errors.addAll(subtitleMover.moveSubs(subsMoveOperation));

        if (errors.isEmpty()) {
            try {
                cleanSourceMediaDir(fileGroup.path());
            } catch (IOException e) {
                LOGGER.warn("Could not clean source media folder", e);
                errors.add(new MediaMoveError(fileGroup.path(), e.getMessage()));
            }
        }

        return errors;
    }

    private void cleanSourceMediaDir(String path) throws IOException {
        for (String folder : importantFolders) {
            if (path.equals(folder)) {
                return;
            }
        }

        for (String restrictedPath : moveConfig.restrictedRemovePaths()) {
            if (path.contains(restrictedPath)) {
                return;
            }
        }

        LocalPath removePath = fileService.toLocalPath(path);
        fileService.deleteDirectory(removePath);
    }

    private boolean movieExists(String movieName, MediaFileType type) {
        if (type == MediaFileType.MOVIE) {
            LocalPath moviePath = fileService.toLocalPath(filesystemConfig.moviesPath(), movieName);
            return Files.exists(moviePath.path()) && Files.isDirectory(moviePath.path());
        }
        return false;
    }
}
