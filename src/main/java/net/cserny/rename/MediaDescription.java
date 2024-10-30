package net.cserny.rename;

import net.cserny.generated.MediaDescriptionData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record MediaDescription(String posterUrl, String title, String date, String description, List<String> cast)
        implements Comparable<MediaDescription> {

    private static final Pattern titleRegex = Pattern
            .compile("^\\s*(?<name>[a-zA-Z0-9-\\s]+)\\s\\((?<date>(\\d{4})(-\\d{1,2}-\\d{1,2})?)\\)$");

    public static List<MediaDescriptionData> generateDescFrom(List<String> titles) {
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

    @Override
    public int compareTo(MediaDescription o) {
        // irrelevant comparator just to be able to sort
        return toString().compareTo(o.toString());
    }
}
