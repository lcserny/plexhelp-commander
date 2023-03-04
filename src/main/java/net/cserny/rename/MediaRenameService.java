package net.cserny.rename;

import net.cserny.rename.NameNormalizer.NameYear;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class MediaRenameService {

    // TODO: probably move to online searcher
    private final Pattern releaseDateRegex = Pattern.compile("\s+\\(\\d{4}(-\\d{2}-\\d{2})?\\)$");

    @Inject
    OnlineSearcher searcher;

    @Inject
    NameNormalizer normalizer;

    @Inject
    DiskSearcher diskSearcher;

    public RenamedMediaOptions produceNames(String name, MediaFileType type) {
        NameYear nameYear = normalizer.normalize(name);
        // TODO: find on disk by name only
        // TODO: find in cache
        // TODO: find in TMDB (cache results)
        // TODO: return nameYear like <name> (<year>)
        return new RenamedMediaOptions(MediaRenameOrigin.NAME, generateDescFrom(List.of(nameYear)));
    }

    private List<MediaDescription> generateDescFrom(List<NameYear> nameYears) {
        return nameYears.stream()
                .map(nameYear -> new MediaDescription(
                        null, normalizer.formatNameYear(nameYear), null, new ArrayList<>()))
                .collect(Collectors.toList());
    }
}
