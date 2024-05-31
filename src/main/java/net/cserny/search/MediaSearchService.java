package net.cserny.search;

import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
@Slf4j
public class MediaSearchService {

    @Autowired
    LocalFileService fileService;

    @Autowired
    FilesystemProperties filesystemConfig;

    @Autowired
    SearchProperties searchConfig;

    public List<MediaFileGroup> findMedia() {
        LocalPath walkPath = fileService.toLocalPath(filesystemConfig.getDownloadsPath());
        try {
            List<Path> files = fileService.walk(walkPath, searchConfig.getMaxDepth());

            List<Path> allVideos = files.stream()
                    .filter(this::excludeConfiguredPaths)
                    .filter(this::excludeNonVideosByContentType)
                    .filter(this::excludeNonVideosBySize)
                    .sorted()
                    .toList();

            return generateMediaFileGroups(allVideos);
        } catch (IOException e) {
            log.warn("Could not walk path " + walkPath.path(), e);
            return Collections.emptyList();
        }
    }

    private List<MediaFileGroup> generateMediaFileGroups(List<Path> allVideos) {
        List<MediaFileGroup> mediaFileGroups = new ArrayList<>();

        Path downloadsPath = fileService.toLocalPath(filesystemConfig.getDownloadsPath()).path();
        int downloadsPathSegments = downloadsPath.getNameCount();

        Map<Pair<String, String>, List<String>> tmpMap = new TreeMap<>();

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
                name = fileService.toLocalPath(nameString.substring(0, nameString.lastIndexOf("."))).path();
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
        for (String excludePath : searchConfig.getExcludePaths()) {
            if (path.toAbsolutePath().toString().contains(excludePath)) {
                log.info("Excluding based on path: " + path.toString());
                return false;
            }
        }
        return true;
    }

    private boolean excludeNonVideosByContentType(Path path) {
        String mimeType;

            Optional<MediaType> mimeTypeOptional = MediaTypeFactory.getMediaType(path.toString());
        if (mimeTypeOptional.isPresent()) {
            mimeType = mimeTypeOptional.get().toString();
         } else {
            log.warn("Could not get content type of file " + path);
            return false;
        }

        for (String allowedType : searchConfig.getVideoMimeTypes()) {
            if (allowedType.equals(mimeType)) {
                return true;
            }
        }

        var result = mimeType != null && mimeType.startsWith("video/");
        if (!result) {
            log.info("Excluding based on mime: " + path.toString() + " - " + mimeType);
        }

        return result;
    }

    private boolean excludeNonVideosBySize(Path path) {
        try {
            long size = Files.size(path);
            var result = size >= searchConfig.getVideoMinSizeBytes();
            if (!result) {
                log.info("Excluding based on size: " + path.toString() + " - " + size);
            }
            return result;
        } catch (IOException e) {
            log.warn("Could not get size of file " + path, e);
            return false;
        }
    }
}
