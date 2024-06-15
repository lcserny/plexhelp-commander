package net.cserny.rename;

import lombok.extern.slf4j.Slf4j;
import net.cserny.rename.NameNormalizer.NameYear;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.cserny.rename.MediaDescription.generateDescFrom;

@Slf4j
@Service
public class MediaRenameService {

    @Autowired
    NameNormalizer normalizer;

    @Autowired
    List<Searcher> searchers;

    public RenamedMediaOptions produceNames(String name, MediaFileType type) {
        NameYear nameYear = normalizer.normalize(name);
        log.info("Normalized media: {}", nameYear);

        for (Searcher searcher : searchers) {
            log.info("Searching options using: {}", searcher);
            RenamedMediaOptions options = searcher.search(nameYear, type);
            if (options.descriptionsFound()) {
                log.info("Found options: {}", options);
                return options;
            }
        }

        List<MediaDescription> mediaDescriptions = generateDescFrom(List.of(nameYear.formatted()));
        log.info("Using options from name with descriptions: {}", mediaDescriptions);
        return new RenamedMediaOptions(MediaRenameOrigin.NAME, mediaDescriptions);
    }
}
