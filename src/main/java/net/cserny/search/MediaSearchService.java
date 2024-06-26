package net.cserny.search;

import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    @Autowired
    MediaIdentificationService identificationService;

    public List<MediaFileGroup> findMedia() {
        LocalPath walkPath = fileService.toLocalPath(filesystemConfig.getDownloadsPath());
        try {
            List<Path> files = fileService.walk(walkPath, searchConfig.getMaxDepth());

            List<Path> allVideos = files.stream()
                    .filter(identificationService::isMedia)
                    .sorted()
                    .toList();

            return generateMediaFileGroups(allVideos);
        } catch (IOException e) {
            log.warn("Could not walk path " + walkPath.path(), e);
            return Collections.emptyList();
        }
    }

    public List<MediaFileGroup> generateMediaFileGroups(List<Path> allVideos) {
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

        log.info("Generated media file groups: {}", mediaFileGroups);

        return mediaFileGroups;
    }
}
