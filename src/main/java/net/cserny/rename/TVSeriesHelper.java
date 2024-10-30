package net.cserny.rename;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TVSeriesHelper {

    private static final Pattern seasonRegex = Pattern.compile("([sS]|Season.|season.)(?<season>[0-9]{1,2})");
    private static final Pattern episodeRegex = Pattern.compile("[eE](?<episode>[0-9]{1,2})");

    private TVSeriesHelper() {
        // no instantiation allowed
    }

    public static Integer findSeason(String seriesName) {
        Matcher matcher = seasonRegex.matcher(seriesName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group("season"));
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
