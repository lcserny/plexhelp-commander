package net.cserny.move.automove;

import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import net.cserny.move.automove.AutoMoveMediaService.AutoMoveOption;
import net.cserny.rename.NameNormalizer.NameYear;
import net.cserny.rename.TVDataExtractor;

import java.util.Comparator;

class MediaTypeComparatorProvider {

    private final boolean yearPresent;
    private final boolean hasTvData;

    MediaTypeComparatorProvider(MediaFileGroup group, NameYear nameYear) {
        this.yearPresent = nameYear.year() != null;

        Integer season = TVDataExtractor.findSeason(group.getName());
        Integer episode = TVDataExtractor.findEpisode(group.getName());
        this.hasTvData = (season != null && season > 0) || (episode != null && episode > 0);
    }

    /**
     * If TV data found (season or episode) then its most likely a TV show, if not and year is
     * present, then its most likely a movie.
     *
     * @return the media comparator to use
     */
    Comparator<AutoMoveOption> provide() {
        return (o1, o2) -> {
            boolean o1IsMovie = o1.type() == MediaFileType.MOVIE;
            boolean o1IsTv = o1.type() == MediaFileType.TV;
            boolean o2IsMovie = o2.type() == MediaFileType.MOVIE;
            boolean o2IsTv = o2.type() == MediaFileType.TV;

            if (hasTvData) {
                if (o1IsTv) {
                    return o2IsTv ? 0 : -1;
                }
                if (o2IsTv) {
                    return 1;
                }
            }

            if (yearPresent) {
                if (o1IsMovie) {
                    return o2IsMovie ? 0 : -1;
                }
                if (o2IsMovie) {
                    return 1;
                }
            } else {
                if (o1IsTv) {
                    return o2IsTv ? 0 : -1;
                }
                if (o2IsTv) {
                    return 1;
                }
            }

            return 0;
        };
    }
}
