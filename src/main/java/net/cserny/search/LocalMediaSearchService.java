package net.cserny.search;

import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class LocalMediaSearchService {

    private static final Logger LOGGER = Logger.getLogger(LocalMediaSearchService.class);

    @Inject
    LocalFileService fileService;

    @Inject
    FilesystemConfig filesystemConfig;

    @Inject
    SearchConfig searchConfig;

    public void setFileService(LocalFileService fileService) {
        this.fileService = fileService;
    }

    public List<Path> findMedia() {
        LocalPath walkPath = fileService.produceLocalPath(filesystemConfig.downloadsPath());
        try {
            List<Path> files = fileService.walk(walkPath, searchConfig.maxDepth());
            return files.stream()
                    .filter(this::excludeConfiguredPaths)
                    .filter(this::excludeNonVideosByContentType)
                    .filter(this::excludeNonVideosBySize)
                    .toList();
        } catch (IOException e) {
            LOGGER.warn("Could not walk path " + walkPath.path(), e);
            return Collections.emptyList();
        }
    }

    private boolean excludeConfiguredPaths(Path path) {
        for (String excludePath : searchConfig.excludePaths()) {
            if (path.toAbsolutePath().toString().contains(excludePath)) {
                return false;
            }
        }
        return true;
    }

    private boolean excludeNonVideosByContentType(Path path) {
        String mimeType;

        try {
            mimeType = Files.probeContentType(path);
        } catch (IOException e) {
            LOGGER.warn("Could not get content type of file " + path, e);
            return false;
        }

        for (String allowedType : searchConfig.videoMimeTypes()) {
            if (allowedType.equals(mimeType)) {
                return true;
            }
        }

        return mimeType != null && mimeType.startsWith("video/");
    }

    private boolean excludeNonVideosBySize(Path path) {
        try {
            long size = Files.size(path);
            return size >= searchConfig.videoMinSizeInBytes();
        } catch (IOException e) {
            LOGGER.warn("Could not get size of file " + path, e);
            return false;
        }
    }
}
