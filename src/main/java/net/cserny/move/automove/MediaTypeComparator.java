package net.cserny.move.automove;

import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import net.cserny.move.automove.AutoMoveMediaService.AutoMoveOption;
import net.cserny.rename.NameNormalizer.NameYear;
import net.cserny.rename.TVDataExtractor;

import java.util.Comparator;

class MediaTypeComparator {

    private MediaTypeComparator() {}

    /**
     * If mediaGroup has multiple videos, then it's a TV Show
     * If mediaGroup name contains season or episode pattern, then it's a TV Show
     * If nameYear has year, then its most likely a Movie
     */
    static Comparator<AutoMoveOption> provide(MediaFileGroup group, NameYear nameYear) {
        boolean yearPresent = nameYear.year() != null;

        Integer season = TVDataExtractor.findSeason(group.getName());
        Integer episode = TVDataExtractor.findEpisode(group.getName());
        boolean hasTvData = group.getVideos().size() > 1
                || (season != null && season > 0)
                || (episode  != null && episode > 0);

        MediaFileType preferred = (hasTvData || !yearPresent) ? MediaFileType.TV : MediaFileType.MOVIE;
        return Comparator.comparingInt(opt -> opt.type() == preferred ? 0 : 1);
    }
}
