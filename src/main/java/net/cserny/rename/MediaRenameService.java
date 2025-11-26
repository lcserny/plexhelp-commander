package net.cserny.rename;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.MediaDescriptionData;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaRenameOrigin;
import net.cserny.generated.RenamedMediaOptions;
import net.cserny.rename.NameNormalizer.NameYear;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.cserny.CommanderApplication.toOneLineString;

@RequiredArgsConstructor
@Slf4j
@Service
public class MediaRenameService {

    private static final Pattern titleRegex = Pattern
            .compile("^\\s*(?<name>[a-zA-Z0-9-\\s]+)\\s\\((?<date>(\\d{4})(-\\d{1,2}-\\d{1,2})?)\\)$");

    private final NameNormalizer normalizer;

    @Getter
    private final List<Searcher> searchers;

    public static List<MediaDescriptionData> generateDescDataFrom(List<String> titles) {
        return titles.stream()
                .map(title -> {
                    String parsedTitle = title;
                    String date = null;

                    Matcher matcher = titleRegex.matcher(title);
                    if (matcher.matches()) {
                        parsedTitle = matcher.group("name");
                        date = matcher.group("date");
                    }

                    return new MediaDescriptionData()
                            .title(parsedTitle)
                            .date(date)
                            .cast(new ArrayList<>());
                })
                .collect(Collectors.toList());
    }

    public RenamedMediaOptions produceNames(String name, MediaFileType type) {
        NameYear nameYear = normalizer.normalize(name);
        log.info("Normalized media: {}", nameYear.formatted());

        for (Searcher searcher : searchers) {
            log.info("Searching options using: {}", searcher.getClass().getSimpleName());
            RenamedMediaOptions options = searcher.search(nameYear, type);
            if (options.getMediaDescriptions() != null && !options.getMediaDescriptions().isEmpty()) {
                log.info("Found options: {}", toOneLineString(options));
                return options;
            }
        }

        List<MediaDescriptionData> mediaDescriptions = generateDescDataFrom(List.of(nameYear.formatted()));
        log.info("Using options from name with descriptions: {}", toOneLineString(mediaDescriptions));
        return new RenamedMediaOptions().origin(MediaRenameOrigin.NAME).mediaDescriptions(mediaDescriptions);
    }
}
