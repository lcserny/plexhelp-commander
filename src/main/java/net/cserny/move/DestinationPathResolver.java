package net.cserny.move;

import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import net.cserny.rename.TVSeriesHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class DestinationPathResolver {

    private static final String SEASON_SUBDIR = "Season ";

    private final LocalFileService fileService;

    @Autowired
    public DestinationPathResolver(LocalFileService fileService) {
        this.fileService = fileService;
    }

    // TODO refactor this
    public LocalPath resolve(MediaFileGroup group, MediaFileType type, String destRoot, String videoName) {
        String ext = videoName.substring(videoName.lastIndexOf("."));
        String newVideoName = group.getName();

        String seasonSubdir = "";
        if (type == MediaFileType.TV) {
            String namePart = newVideoName;
            String datePart = "";
            int dateStartIndex = newVideoName.lastIndexOf(" (");
            if (dateStartIndex != -1) {
                namePart = newVideoName.substring(0, dateStartIndex);
                datePart = newVideoName.substring(dateStartIndex);
            }

            String additionalSpace = " ";
            String seasonPart = "";
            Integer season = group.getSeason();
            Integer videoSeason = TVSeriesHelper.findSeason(videoName);
            if (season == null && videoSeason != null) {
                season = videoSeason;
            }

            if (season != null) {
                if (videoSeason != null && !videoSeason.equals(season)) {
                    season = videoSeason;
                }

                seasonSubdir = SEASON_SUBDIR + season;
                additionalSpace = "";
                seasonPart = format(" S%02d", season);
            }

            Integer episodeNr = TVSeriesHelper.findEpisode(videoName);
            String episodePart = "";
            if (episodeNr != null) {
                episodePart = format("%sE%02d", additionalSpace, episodeNr);
            }

            newVideoName = namePart + seasonPart + episodePart + datePart;
        }

        newVideoName += ext;

        return fileService.toLocalPath(destRoot, group.getName(), seasonSubdir, newVideoName);
    }
}
