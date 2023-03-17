package net.cserny.move;

import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalFileService.WalkOptions;
import net.cserny.filesystem.LocalPath;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class SubtitleMover {

    private static final Logger LOGGER = Logger.getLogger(SubtitleMover.class);

    @Inject
    LocalFileService fileService;

    @Inject
    FilesystemConfig filesystemConfig;

    @Inject
    MoveConfig moveConfig;

    public List<MediaMoveError> moveSubs(SubsMoveOperation operation) {
        if (operation.subsSrc().toString().equals(filesystemConfig.downloadsPath())) {
            LOGGER.info("Path to move subs is root Downloads path, skipping operation...");
            return Collections.emptyList();
        }

        List<MediaMoveError> errors = new ArrayList<>();

        List<Path> subs;
        try {
            subs = fileService.walk(operation.subsSrc(), moveConfig.subsMaxDepth(), WalkOptions.ONLY_FILES)
                    .stream()
                    .filter(this::filterBySubExtension)
                    .toList();
        } catch (IOException e) {
            LOGGER.warn("Could not walk subs path", e);
            errors.add(new MediaMoveError(operation.subsSrc().path().toString(), e.getMessage()));
            return errors;
        }

        if (subs.isEmpty()) {
            LOGGER.info("No subs found for media " + operation.subsSrc().path());
            return Collections.emptyList();
        }

        errors.addAll(switch (operation.type()) {
            case MOVIE -> moveMovieSubs(operation, subs);
            case TV -> moveTvSubs(operation, subs);
        });

        return errors;
    }

    private List<MediaMoveError> moveTvSubs(SubsMoveOperation operation, List<Path> subs) {
        List<MediaMoveError> errors = new ArrayList<>();
        // create Subs folder in subsDest
            // for each file
                // go over each segment of path, if one segment contains e99 prepend that segment to the file name
                // move file to Subs folder with new name
        return errors;
    }

    private List<MediaMoveError> moveMovieSubs(SubsMoveOperation operation, List<Path> subs) {
        List<MediaMoveError> errors = new ArrayList<>();
        for (Path sub : subs) {
            LocalPath subSrc = fileService.produceLocalPath(sub.toString());
            String subFilename = sub.getFileName().toString();
            LocalPath subDest = fileService.produceLocalPath(operation.subsDest().path().toString(), subFilename);

            try {
                fileService.move(subSrc, subDest);
            } catch (IOException e) {
                LOGGER.warn("Could not move sub", e);
                errors.add(new MediaMoveError(subSrc.path().toString(), e.getMessage()));
            }
        }
        return errors;
    }

    private boolean filterBySubExtension(Path subPath) {
        String filename = subPath.getFileName().toString();
        String ext = filename.substring(filename.lastIndexOf("."));

        for (String subsExtension : moveConfig.subsExtensions()) {
            if (ext.equals(subsExtension)) {
                return true;
            }
        }

        return false;
    }
}
