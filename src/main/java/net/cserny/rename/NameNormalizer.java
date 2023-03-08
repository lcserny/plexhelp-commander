package net.cserny.rename;

import org.apache.commons.text.WordUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class NameNormalizer {

    @Inject
    RenameConfig renameConfig;

    private final Pattern preNormalizedNameRegex = Pattern.compile("^\s*(?<name>[a-zA-Z0-9-\s]+)\s\\((?<year>\\d{4})(-\\d{1,2}-\\d{1,2})?\\)$");
    private final Pattern specialCharsRegex = Pattern.compile("[^a-zA-Z0-9-\s]");
    private final Pattern spaceMergeRegex = Pattern.compile("\s{2,}");
    private final Pattern yearRegex = Pattern.compile("\s\\d{4}$");

    private final List<Pattern> nameTrimPatterns = new ArrayList<>();

    @PostConstruct
    public void init() {
        for (String regex : renameConfig.trimRegexList()) {
            nameTrimPatterns.add(Pattern.compile(regex));
        }
    }

    public NameYear normalize(String name) {
        Matcher preNormMatcher = preNormalizedNameRegex.matcher(name);
        if (preNormMatcher.matches()) {
            name = preNormMatcher.group("name").trim();
            Integer year = Integer.parseInt(preNormMatcher.group("year"));
            return new NameYear(name, year);
        }

        for (Pattern regex : nameTrimPatterns) {
            Matcher matcher = regex.matcher(name);
            if (matcher.find()) {
                name = name.substring(0, matcher.start());
            }
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

    public record NameYear(String name, Integer year) {

        public String formatted() {
            return name() + (year != null ? " (" + year + ")" : "");
        }
    }
}
