package net.cserny.search;

import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Singleton
public class LocalMediaSearchService {

    private static final Logger LOGGER = Logger.getLogger(LocalMediaSearchService.class);

    @Inject
    LocalFileService fileService;

    @Inject
    FilesystemConfig filesystemConfig;

    @Inject
    SearchConfig searchConfig;

    public List<MediaFile> findMedia() {
        LocalPath walkPath = fileService.produceLocalPath(filesystemConfig.downloadsPath());
        try {
            List<Path> files = fileService.walk(walkPath, searchConfig.maxDepth());

            List<Path> allVideos = files.stream()
                    .filter(this::excludeConfiguredPaths)
                    .filter(this::excludeNonVideosByContentType)
                    .filter(this::excludeNonVideosBySize)
                    .sorted()
                    .toList();

            return generateMediaFiles(allVideos);
        } catch (IOException e) {
            LOGGER.warn("Could not walk path " + walkPath.path(), e);
            return Collections.emptyList();
        }
    }

    // TODO
    // group videos by parent path, generate name, create MediaFiles
    // if downloads path = parent path, leave name null
    private List<MediaFile> generateMediaFiles(List<Path> allVideos) {
        List<MediaFile> mediaFiles = new ArrayList<>();

        String downloadsPath = filesystemConfig.downloadsPath();
        for (Path videoPath : allVideos) {
            String path = downloadsPath;

            String name = videoPath.toString();
            name = name.substring(downloadsPath.length());
            name = name.substring(0, name.indexOf(videoPath.getFileName().toString()));
            if (!name.matches(Pattern.quote(File.separator) + "*")) {
                name = name.substring(0,
                        name.indexOf(File.separator, name.indexOf(File.separator) + 1));
                name = name.replaceAll(Pattern.quote(File.separator), "");
                path = Paths.get(downloadsPath, name).toString();
            } else {
                name = null;
            }

            String remainingVideoPath = videoPath.toString().substring(path.length());

            // TODO: you have name, path and videoFile, now group these
        }

        return mediaFiles;
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
