package net.cserny.rename;

import io.quarkus.arc.All;
import net.cserny.rename.NameNormalizer.NameYear;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static net.cserny.rename.MediaDescription.generateDescFrom;

@Singleton
public class MediaRenameService {

    @Inject
    NameNormalizer normalizer;

    @Inject
    @All
    List<Searcher> searchers;

    public RenamedMediaOptions produceNames(String name, MediaFileType type) {
        NameYear nameYear = normalizer.normalize(name);

        for (Searcher searcher : searchers) {
            RenamedMediaOptions options = searcher.search(nameYear, type);
            if (options.descriptionsFound()) {
                return options;
            }
        }

        return new RenamedMediaOptions(MediaRenameOrigin.NAME, generateDescFrom(List.of(nameYear.formatted())));
    }
}
