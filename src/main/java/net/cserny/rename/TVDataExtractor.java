package net.cserny.rename;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TVDataExtractor {

    private static final Pattern seasonRegex = Pattern.compile("((?<season1>[0-9]{1,2})(nd|rd|st|th)\\s[sS]eason)|(([sS](eason[\\s.]{0,2})?)(?<season2>[0-9]{1,2}))");
    private static final Pattern episodeRegex = Pattern.compile("([eE]|[sS]eason\\s-\\s)(?<episode>[0-9]{1,2})");

    public static Integer findSeason(String seriesName) {
        Matcher matcher = seasonRegex.matcher(seriesName);

        String foundSeason = null;
        int matchesCount = 0;
        while (matcher.find()) {
            String firstSeasonRegex = matcher.group("season1");
            String secondSeasonRegex = matcher.group("season2");
            foundSeason = firstSeasonRegex != null ? firstSeasonRegex : secondSeasonRegex;
            matchesCount++;
        }

        if (matchesCount > 1) {
            log.info("Found {} seasons in series {}", matchesCount, seriesName);
            return null;
        }

        if (foundSeason != null) {
            return Integer.parseInt(foundSeason);
        }

        return null;
    }

    public static Integer findEpisode(String seriesName) {
        Matcher matcher = episodeRegex.matcher(seriesName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group("episode"));
        }
        return null;
    }
}
