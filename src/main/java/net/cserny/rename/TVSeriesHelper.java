package net.cserny.rename;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TVSeriesHelper {

    private static final Pattern seasonRegex = Pattern.compile("([sS]|Season.|season.)(?<season>[0-9]{1,2})");
    private static final Pattern episodeRegex = Pattern.compile("[eE](?<episode>[0-9]{1,2})");

    public static Integer findSeason(String seriesName) {
        Matcher matcher = seasonRegex.matcher(seriesName);

        String foundSeason = null;
        int matchesCount = 0;
        while (matcher.find()) {
            foundSeason = matcher.group("season");
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
