package net.cserny.rename;

import net.cserny.rename.NameNormalizer.NameYear;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.cserny.rename.MediaDescription.generateDescFrom;

@Service
public class MediaRenameService {

    @Autowired
    NameNormalizer normalizer;

    @Autowired
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
