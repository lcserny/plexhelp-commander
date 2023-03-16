package net.cserny.move;

import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
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

        // TODO
        // go over all files in subsSrc
        // filter files by configured sub extension
        // save them to collection

        // if collection is not empty
            // if movie
                // move all files from collection to subsDest
            // if tv
                // create Subs folder in subsDest
                // for each file
                    // go over each segment of path, if one segment contains e99 prepend that segment to the file name
                    // move file to Subs folder with new name


        return null;
    }
}
