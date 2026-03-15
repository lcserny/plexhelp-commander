package net.cserny.api.dto;

import lombok.Builder;
import net.cserny.generated.MediaFileType;

import java.time.LocalDate;

@Builder
public record MediaInfo(String baseName, LocalDate date, Integer season, Integer episode, String fileName, MediaFileType type) {

    public static final String SEASON_SUBSTR = "Season";

    // MOVIE [baseName + (date), fileName]
    // TV [baseName + (date), Season %20d(season), fileName]
    public String[] destinationPathSegments() {
        String baseNameAndDate = date == null ? baseName : baseName + " (" + date + ")";
        return switch (type) {
            case MOVIE -> new String[]{ baseNameAndDate, fileName };
            case TV -> season == null
                    ? new String[]{ baseNameAndDate, fileName }
                    : new String[]{ baseNameAndDate, SEASON_SUBSTR + " " + season, fileName };
        };
    }
}