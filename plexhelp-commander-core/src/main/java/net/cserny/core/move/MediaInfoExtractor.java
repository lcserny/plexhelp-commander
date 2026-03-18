package net.cserny.core.move;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.MediaFileType;
import net.cserny.core.move.MediaGrouper.LangKey;
import net.cserny.api.dto.MediaInfo;
import net.cserny.support.TVDataExtractor;

import java.time.LocalDate;

@Slf4j
public class MediaInfoExtractor {

    private final String groupName;
    private final Integer season;
    private final MediaFileType type;
    private final String mediaName;
    private final IndexedLangData langData;

    public MediaInfoExtractor(String groupName, Integer season, MediaFileType type, String mediaName, IndexedLangData langData) {
        this.groupName = groupName;
        this.season = season;
        this.type = type;
        this.mediaName = mediaName;
        this.langData = langData;
    }

    public MediaInfoExtractor(String groupName, Integer season, MediaFileType type, String mediaName) {
        this(groupName, season, type, mediaName, null);
    }

    public MediaInfoExtractor(String groupName, MediaFileType type, String mediaName) {
        this(groupName, null, type, mediaName, null);
    }

    public MediaInfo extractMediaInfo() {
        LocalDate localDate = extractDate();
        String baseName = extractBaseName(localDate != null);
        Integer season = extractSeason();
        Integer episode = extractEpisode();
        String fileName = extractFileName(baseName, localDate, season, episode);

        return MediaInfo.builder().baseName(baseName).date(localDate).season(season).episode(episode).fileName(fileName).type(type).build();
    }

    private String extractBaseName(boolean datePresent) {
        if (!datePresent) {
            return groupName;
        } else {
            return groupName.substring(0, groupName.lastIndexOf(" ("));
        }
    }

    private LocalDate extractDate() {
        int dateStartIndex = groupName.lastIndexOf(" (");
        int dateEndIndex = groupName.lastIndexOf(")");

        if (dateStartIndex != -1 && dateEndIndex != -1) {
            String substring = groupName.substring(dateStartIndex + 2, dateEndIndex);
            try {
                return LocalDate.parse(substring);
            } catch (Exception e) {
                log.debug("Could not parse date from: {}", substring, e);
                return null;
            }
        }

        return null;
    }

    private Integer extractSeason() {
        if (this.type != MediaFileType.TV) {
            return null;
        }

        Integer videoSeason = TVDataExtractor.findSeason(mediaName);
        if (videoSeason != null && !videoSeason.equals(this.season)) {
            return videoSeason;
        }

        return this.season;
    }

    private Integer extractEpisode() {
        if (this.type != MediaFileType.TV) {
            return null;
        }

        return TVDataExtractor.findEpisode(mediaName);
    }

    private String extractExtension() {
        return mediaName.substring(mediaName.lastIndexOf("."));
    }

    private String extractIndexedLangData() {
        if (langData == null) {
            return "";
        }

        boolean noLangCode = LangKey.NO_LANG.equals(langData.lang().iso2Code());
        if (langData.singleMediaForLang()) {
            if (noLangCode) {
                return "";
            } else {
                return "." + langData.lang().iso2Code();
            }
        }

        if (noLangCode) {
            return ".(" + langData.indexNr() + ")";
        }

        return "." + langData.lang().iso2Code() + ".(" + langData.indexNr() + ")";
    }

    // fileName = baseName + [S0x][E0y] + (year) + [lang data and index] + extension
    private String extractFileName(String baseName, LocalDate localDate,  Integer season, Integer episode) {
        return baseName +
                (season != null ? " S%02d".formatted(season) : "") +
                (episode != null ? "E%02d".formatted(episode) : "") +
                (localDate != null ? " (" + localDate.getYear() + ")" : "") +
                extractIndexedLangData() +
                extractExtension();
    }

    @Builder
    public record IndexedLangData(LangKey lang, boolean singleMediaForLang, int indexNr) {}
}
