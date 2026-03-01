package net.cserny.task.clean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.LocalPathHandler;
import net.cserny.api.MediaIdentifier;
import net.cserny.api.WalkOptions;
import net.cserny.api.dto.LocalPath;
import net.cserny.config.FilesystemProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanEmptyService {

    private final FilesystemProperties filesystemProperties;
    private final MediaIdentifier mediaIdentifier;
    private final LocalPathHandler localPathHandler;

    public void run() throws IOException {
        List<String> pathsToClean = List.of(filesystemProperties.getTvPath(), filesystemProperties.getMoviesPath());
        for (String path : pathsToClean) {
            clean(path);
        }
    }

    private void clean(String path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Path provided is null");
        }

        LocalPath walkPath = localPathHandler.toLocalPath(path);
        List<LocalPath> foldersFound = localPathHandler.walk(walkPath, 2, WalkOptions.ONLY_DIRECTORIES);

        // start index at 1 to exclude first path cause its the root path used
        for (int i = 1; i < foldersFound.size(); i++) {
            LocalPath mediaFolder = foldersFound.get(i);

            log.info("Checking media folder {} for media files", mediaFolder);
            List<LocalPath> filesInMediaFolder = localPathHandler.walk(mediaFolder, 4, WalkOptions.ONLY_FILES);

            boolean containsMediaFiles = false;
            for (LocalPath file : filesInMediaFolder) {
                if (mediaIdentifier.isMedia(file)) {
                    containsMediaFiles = true;
                    break;
                }
            }

            if (!containsMediaFiles) {
                log.info("No files found in media folder {}, deleting...", mediaFolder);
                localPathHandler.deleteDirectory(mediaFolder);
            }
        }
    }
}
