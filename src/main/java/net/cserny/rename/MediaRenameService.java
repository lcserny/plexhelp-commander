package net.cserny.rename;

import net.cserny.rename.NameNormalizer.NameYear;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class MediaRenameService {

    @Inject
    CacheSearcher onlineSearcher;

    @Inject
    NameNormalizer normalizer;

    @Inject
    DiskSearcher diskSearcher;

    public RenamedMediaOptions produceNames(String name, MediaFileType type) {
        NameYear nameYear = normalizer.normalize(name);
        List<String> foundList = diskSearcher.search(nameYear, type);
        if (!foundList.isEmpty()) {
            return new RenamedMediaOptions(MediaRenameOrigin.DISK, generateDescFrom(foundList));
        }
        // TODO: find in cache
        // TODO: find in TMDB (cache results)
        // TODO: return nameYear like <name> (<year>)
        return new RenamedMediaOptions(MediaRenameOrigin.NAME, generateDescFrom(List.of(nameYear.formatted())));
    }

    private List<MediaDescription> generateDescFrom(List<String> titles) {
        return titles.stream()
                .map(title -> new MediaDescription(
                        null, title, null, new ArrayList<>()))
                .collect(Collectors.toList());
    }
}
