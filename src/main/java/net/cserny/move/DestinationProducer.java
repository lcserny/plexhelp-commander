package net.cserny.move;

import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import net.cserny.rename.TVSeriesHelper;

import static java.lang.String.format;

public class DestinationProducer {

    private static final String SEASON_SUB_DIR_PAT = "Season %s";

    private final MediaFileGroup group;
    private final MediaFileType type;
    private final String videoName;

    private Integer season;
    private Integer episode;

    public DestinationProducer(MediaFileGroup group, MediaFileType type, String videoName) {
        this.group = group;
        this.type = type;
        this.videoName = videoName;
    }

    private String getExtension() {
        return videoName.substring(videoName.lastIndexOf("."));
    }

    public Integer getSeason() {
        if (this.type != MediaFileType.TV) {
            return null;
        }

        if (this.season == null) {
            Integer season = group.getSeason();
            Integer videoSeason = TVSeriesHelper.findSeason(videoName);
            if (videoSeason != null && !videoSeason.equals(season)) {
                season = videoSeason;
            }
            this.season = season;
        }

        return this.season;
    }

    private String getSeasonPart() {
        Integer season = getSeason();
        if (season != null) {
            return format(" S%02d", season);
        }
        return "";
    }

    public String getSeasonSubDir() {
        Integer season = getSeason();
        if (season != null) {
            return format(SEASON_SUB_DIR_PAT, season);
        }
        return "";
    }

    private Integer getEpisode() {
        if (this.type != MediaFileType.TV) {
            return null;
        }

        if (this.episode == null) {
           this.episode = TVSeriesHelper.findEpisode(videoName);
        }

        return this.episode;
    }

    private String getEpisodePart() {
        Integer episode = getEpisode();
        String additionalSpace = getSeason() == null ? " " : "";
        String episodePart = "";
        if (episode != null) {
            episodePart = format("%sE%02d", additionalSpace, episode);
        }
        return episodePart;
    }

    public String getNewVideoName() {
        return switch (type) {
            case TV -> getNewTVName();
            case MOVIE -> getNewMovieName();
        };
    }

    private String getNewTVName() {
        String newVideoName = group.getName();

        String namePart = newVideoName;
        String datePart = "";
        int dateStartIndex = newVideoName.lastIndexOf(" (");
        if (dateStartIndex != -1) {
            namePart = newVideoName.substring(0, dateStartIndex);
            datePart = newVideoName.substring(dateStartIndex);
        }

        newVideoName = namePart + getSeasonPart() + getEpisodePart() + datePart;

        return newVideoName + getExtension();
    }

    private String getNewMovieName() {
        return group.getName() + getExtension();
    }
}
