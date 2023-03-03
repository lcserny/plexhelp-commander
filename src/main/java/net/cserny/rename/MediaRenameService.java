package net.cserny.rename;

import net.cserny.filesystem.FilesystemConfig;
import org.apache.commons.text.WordUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class MediaRenameService {

    // TODO: do these work ok? check double backlash
    private final Pattern preNormalizedNameRegex = Pattern.compile("^\s*(?<name>[a-zA-Z0-9-\s]+)\s\\((?<year>\\d{4})(-\\d{1,2}-\\d{1,2})?\\)$");
    private final Pattern specialCharsRegex = Pattern.compile("[^a-zA-Z0-9-\s]");
    private final Pattern spaceMergeRegex = Pattern.compile("\s{2,}");
    private final Pattern yearRegex = Pattern.compile("\s\\d{4}$");
    private final Pattern releaseDateRegex = Pattern.compile("\s+\\(\\d{4}(-\\d{2}-\\d{2})?\\)$");

    @Inject
    OnlineSearchService onlineSearchService;

    @Inject
    RenameConfig renameConfig;

    @Inject
    FilesystemConfig filesystemConfig;

    public RenamedMediaOptions produceNames(String name, MediaFileType type) {
        NameYear nameYear = normalize(name);
        // TODO: find on disk by name only
        // TODO: find in cache
        // TODO: find in TMDB (cache results)
        // TODO: return nameYear like <name> (<year>)
        return null;
    }

    // TODO: check if this works
    private NameYear normalize(String name) {
        Matcher preNormMatcher = preNormalizedNameRegex.matcher(name);
        if (preNormMatcher.matches()) {
            name = preNormMatcher.group("name").trim();
            Integer year = Integer.parseInt(preNormMatcher.group("year"));
            return new NameYear(name, year);
        }

        for (String regex : renameConfig.trimRegexList()) {
            name = name.replaceAll(regex, "");
        }

        name = name.replaceAll("&", "and");
        name = name.replaceAll(specialCharsRegex.pattern(), " ");
        name = name.replaceAll(spaceMergeRegex.pattern(), " ");
        name = name.trim();
        name = WordUtils.capitalize(name);

        Integer year = null;
        Matcher yearMatcher = yearRegex.matcher(name);
        if (yearMatcher.find()) {
            int startIndex = yearMatcher.start();
            year = Integer.valueOf(name.substring(startIndex + 1));
            name = name.substring(0, startIndex);
        }

        return new NameYear(name, year);
    }

    private record NameYear(String name, Integer year) {
    }
}
