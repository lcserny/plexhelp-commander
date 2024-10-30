package net.cserny.move;

import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DefaultVideosParser implements VideosParser {

    @Autowired
    LocalFileService fileService;

    @Override
    public ParsedVideos parse(MediaFileGroup fileGroup, MediaFileType type) {
        List<String> videos = new ArrayList<>(fileGroup.getVideos());
        List<LocalPath> deletableVideos = new ArrayList<>();

        if (type == MediaFileType.MOVIE && videos.size() > 1) {
            log.info("Movie has a large sample file also, processing it out {}", fileGroup.getName());
            updateVideosForLargeSampleFile(videos, deletableVideos, fileGroup.getPath());
        }

        return new ParsedVideos(videos, deletableVideos);
    }

    private void updateVideosForLargeSampleFile(List<String> videos, List<LocalPath> deletableVideos, String fileGroupPath) {
        List<Pair<String, LocalPath>> list = videos.stream().parallel()
                .map(s -> {
                    LocalPath localPath = fileService.toLocalPath(fileGroupPath, s);
                    return Pair.of(s, localPath);
                })
                .sorted(Comparator.comparingLong(pair -> pair.getRight().attributes().size()))
                .collect(Collectors.toCollection(ArrayList::new));

        videos.clear();
        videos.add(list.removeLast().getLeft());

        list.forEach(pair -> deletableVideos.add(pair.getRight()));
    }
}
