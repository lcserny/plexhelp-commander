package net.cserny.search;

import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Singleton
public class LocalMediaSearchService {

    private static final Logger LOGGER = Logger.getLogger(LocalMediaSearchService.class);

    @Inject
    LocalFileService fileService;

    @Inject
    FilesystemConfig filesystemConfig;

    @Inject
    SearchConfig searchConfig;

    public List<MediaFileGroup> findMedia() {
        LocalPath walkPath = fileService.produceLocalPath(filesystemConfig.downloadsPath());
        try {
            List<Path> files = fileService.walk(walkPath, searchConfig.maxDepth());

            List<Path> allVideos = files.stream()
                    .filter(this::excludeConfiguredPaths)
                    .filter(this::excludeNonVideosByContentType)
                    .filter(this::excludeNonVideosBySize)
                    .sorted()
                    .toList();

            return generateMediaFileGroups(allVideos);
        } catch (IOException e) {
            LOGGER.warn("Could not walk path " + walkPath.path(), e);
            return Collections.emptyList();
        }
    }

    private List<MediaFileGroup> generateMediaFileGroups(List<Path> allVideos) {
        List<MediaFileGroup> mediaFileGroups = new ArrayList<>();

        Path downloadsPath = Paths.get(filesystemConfig.downloadsPath());
        int downloadsPathSegments = downloadsPath.getNameCount();

        Map<Pair<String, String>, List<String>> tmpMap = new HashMap<>();

        for (Path videoPath : allVideos) {
            int videoPathSegments = videoPath.getNameCount();

            Path name = videoPath.subpath(downloadsPathSegments, downloadsPathSegments + 1);
            Path path = downloadsPath;
            Path video = name;
            if (videoPathSegments > downloadsPathSegments + 1) {
                path = downloadsPath.resolve(name);
                video = videoPath.subpath(downloadsPathSegments + 1, videoPathSegments);
            } else {
                String nameString = name.toString();
                name = Paths.get(nameString.substring(0, nameString.lastIndexOf(".")));
            }

            Pair<String, String> key = Pair.of(path.toString(), name.toString());
            List<String> videos = tmpMap.get(key);
            if (videos == null) {
                videos = new ArrayList<>();
            }
            videos.add(video.toString());
            tmpMap.put(key, videos);
        }

        for (Map.Entry<Pair<String, String>, List<String>> entry : tmpMap.entrySet()) {
            Pair<String, String> key = entry.getKey();
            List<String> value = entry.getValue();
            MediaFileGroup mediaFileGroup = new MediaFileGroup(key.getLeft(), key.getRight(), value);
            mediaFileGroups.add(mediaFileGroup);
        }

        return mediaFileGroups;
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
