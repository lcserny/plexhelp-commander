package net.cserny.rename;

import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.tv.TvSeries;
import lombok.extern.slf4j.Slf4j;
import net.cserny.rename.NameNormalizer.NameYear;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Order(2)
@Component
@Slf4j
public class TMDBSearcher implements Searcher {

    @Autowired
    OnlineCacheRepository repository;

    @Autowired
    OnlineConfig onlineConfig;

    @Autowired
    TmdbWrapper tmdbWrapper;

    private final Pattern specialCharsRegex = Pattern.compile("[^a-zA-Z0-9-\s]");

    @Override
    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        List<MediaDescription> mediaFound = switch (type) {
            case MOVIE -> searchMovie(nameYear);
            case TV -> searchTvShow(nameYear);
        };

        repository.saveAllOnlineCacheItem(nameYear, mediaFound, type);

        return new RenamedMediaOptions(MediaRenameOrigin.TMDB, mediaFound);
    }

    private List<MediaDescription> searchTvShow(NameYear nameYear) {
        TvResultsPage page = tmdbWrapper.searchTvShows(nameYear.name());
        if (page == null) {
            log.warn("TMDB Search could not search TV, check configuration");
            return Collections.emptyList();
        }

        List<TvSeries> results = page.getResults();
        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        List<TvSeries> sublist = results.subList(0, Math.min(results.size(), onlineConfig.getResultLimit()));

        List<MediaDescription> descriptions = new ArrayList<>();
        for (TvSeries tvSeries : sublist) {
            String posterUrl = producePosterUrl(tvSeries.getPosterPath());
            String title = processTitle(tvSeries.getName());
            String date = tvSeries.getFirstAirDate();
            String description = nullIfBlank(tvSeries.getOverview());
            List<String> cast = produceCast(tmdbWrapper.tvShowCredits(tvSeries.getId()));

            descriptions.add(new MediaDescription( posterUrl, title, date, description, cast ));
        }

        return descriptions;
    }

    private List<MediaDescription> searchMovie(NameYear nameYear) {
        MovieResultsPage page = tmdbWrapper.searchMovies(nameYear.name(), nameYear.year());
        if (page == null) {
            log.warn("TMDB Search could not search Movies, check configuration");
            return Collections.emptyList();
        }

        List<MovieDb> results = page.getResults();
        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        List<MovieDb> sublist = results.subList(0, Math.min(results.size(), onlineConfig.getResultLimit()));

        List<MediaDescription> descriptions = new ArrayList<>();
        for (MovieDb movieDb : sublist) {
            String posterUrl = producePosterUrl(movieDb.getPosterPath());
            String title = processTitle(movieDb.getTitle());
            String date = movieDb.getReleaseDate();
            String description = nullIfBlank(movieDb.getOverview());
            List<String> cast = produceCast(tmdbWrapper.movieCredits(movieDb.getId()));

            descriptions.add(new MediaDescription( posterUrl, title, date, description, cast ));
        }

        return descriptions;
    }

    private String processTitle(String title) {
        return title.replaceAll("&", "and")
                .replaceAll(specialCharsRegex.pattern(), "");
    }

    private String producePosterUrl(String posterPath) {
        return StringUtils.isBlank(posterPath) ? null : onlineConfig.getPosterBase() + posterPath;
    }

    private String nullIfBlank(String text) {
        return StringUtils.isBlank(text) ? null : text;
    }

    private List<String> produceCast(Credits credits) {
        if (credits == null) {
            return Collections.emptyList();
        }

        return credits.getCast().stream()
                .map(PersonCast::getCharacter)
                .limit(onlineConfig.getResultLimit())
                .toList();
    }
}
