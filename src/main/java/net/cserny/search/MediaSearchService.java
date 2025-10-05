package net.cserny.search;

import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;

import net.cserny.generated.MediaFileGroup;
import net.cserny.rename.TVDataExtractor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static net.cserny.Utils.toOneLineString;

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
            List<LocalPath> files = fileService.walk(walkPath, searchConfig.getMaxDepth(), searchConfig.getExcludePaths());

            List<LocalPath> allVideos = files.stream()
                    .filter(identificationService::isMedia)
                    .sorted()
                    .toList();

            return generateMediaFileGroups(allVideos);
        } catch (IOException e) {
            log.warn("Could not walk path {}: {}", walkPath, e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<MediaFileGroup> generateMediaFileGroups(List<LocalPath> allVideos) {
        List<MediaFileGroup> mediaFileGroups = new ArrayList<>();

        Path downloadsPath = fileService.toLocalPath(filesystemConfig.getDownloadsPath()).path();
        int downloadsPathSegments = downloadsPath.getNameCount();

        Map<Pair<String, NameBundle>, List<String>> tmpMap = new TreeMap<>();

        for (LocalPath localPath : allVideos) {
            Path videoPath = localPath.path();
            int videoPathSegments = videoPath.getNameCount();

            boolean noParentDirectory = false;
            Path name = videoPath.subpath(downloadsPathSegments, downloadsPathSegments + 1);
            Path path = downloadsPath;
            Path video = name;
            if (videoPathSegments > downloadsPathSegments + 1) {
                path = downloadsPath.resolve(name);
                video = videoPath.subpath(downloadsPathSegments + 1, videoPathSegments);
            } else {
                noParentDirectory = true;
                String nameString = name.toString();
                name = fileService.toLocalPath(nameString.substring(0, nameString.lastIndexOf("."))).path();
            }

            Pair<String, NameBundle> key = Pair.of(path.toString(), new NameBundle(name.toString(), noParentDirectory));
            List<String> videos = tmpMap.get(key);
            if (videos == null) {
                videos = new ArrayList<>();
            }
            videos.add(video.toString());
            tmpMap.put(key, videos);
        }

        for (Map.Entry<Pair<String, NameBundle>, List<String>> entry : tmpMap.entrySet()) {
            Pair<String, NameBundle> key = entry.getKey();
            List<String> value = entry.getValue();
            MediaFileGroup mediaFileGroup = new MediaFileGroup().path(key.getLeft())
                    .name(key.getRight().name())
                    .noParent(key.getRight().noParentDirectory())
                    .videos(value);
            mediaFileGroup.setSeason(TVDataExtractor.findSeason(mediaFileGroup.getName()));
            mediaFileGroups.add(mediaFileGroup);
        }

        log.info("Generated media file groups: {}", toOneLineString(mediaFileGroups));

        return mediaFileGroups;
    }

    private record NameBundle(String name, boolean noParentDirectory) implements Comparable<NameBundle> {

        @Override
        public int compareTo(MediaSearchService.NameBundle other) {
            return this.name().compareTo(other.name());
        }
    }
}
