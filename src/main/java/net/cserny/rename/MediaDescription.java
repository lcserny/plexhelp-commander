package net.cserny.rename;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record MediaDescription(String posterUrl, String title, String date, String description, List<String> cast) {

    public static List<MediaDescription> generateDescFrom(List<String> titles) {
        return titles.stream()
                .map(title -> new MediaDescription(
                        null, title, null, null, new ArrayList<>()))
                .collect(Collectors.toList());
    }
}
