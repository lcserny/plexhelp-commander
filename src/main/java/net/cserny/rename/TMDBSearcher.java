package net.cserny.rename;

import info.movito.themoviedbapi.*;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.core.NamedIdElement;
import info.movito.themoviedbapi.model.tv.TvSeries;
import net.cserny.rename.NameNormalizer.NameYear;
import org.apache.http.util.TextUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Priority(0)
@Singleton
public class TMDBSearcher implements Searcher {

    @Inject
    OnlineCacheRepository repository;

    @Inject
    TMDBConfig tmdbConfig;

    private TmdbApi tmdbApi;

    private final Pattern specialCharsRegex = Pattern.compile("[^a-zA-Z0-9-\s]");

    @PostConstruct
    public void init() {
        tmdbApi = new TmdbApi(tmdbConfig.apiKey());
    }

    @Override
    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        TmdbSearch tmdbSearch = tmdbApi.getSearch();

        List<MediaDescription> mediaFound = switch (type) {
            case MOVIE -> searchMovie(tmdbSearch, nameYear);
            case TV -> searchTvShow(tmdbSearch, nameYear);
        };

        repository.saveAllOnlineCacheItem(nameYear, mediaFound, type);

        return new RenamedMediaOptions(MediaRenameOrigin.TMDB, mediaFound);
    }

    private List<MediaDescription> searchTvShow(TmdbSearch tmdbSearch, NameYear nameYear) {
        TmdbTV tmdbTV = tmdbApi.getTvSeries();

        TvResultsPage page = tmdbSearch.searchTv(nameYear.name(), null, 0);
        List<TvSeries> results = page.getResults();
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        List<TvSeries> sublist = results.subList(0, Math.min(results.size(), tmdbConfig.resultLimit()));

        List<MediaDescription> descriptions = new ArrayList<>();
        for (TvSeries tvSeries : sublist) {
            String posterUrl = producePosterUrl(tvSeries.getPosterPath());
            String title = processTitle(tvSeries.getName());
            LocalDate date = produceDate(tvSeries.getFirstAirDate());
            String description = nullIfBlank(tvSeries.getOverview());
            List<String> cast = produceCast(tmdbTV.getCredits(tvSeries.getId(), null));

            descriptions.add(new MediaDescription( posterUrl, title, date, description, cast ));
        }

        return descriptions;
    }

    private List<MediaDescription> searchMovie(TmdbSearch tmdbSearch, NameYear nameYear) {
        TmdbMovies tmdbMovies = tmdbApi.getMovies();

        MovieResultsPage page = tmdbSearch.searchMovie(nameYear.name(), nameYear.year(), null, false, 0);
        List<MovieDb> results = page.getResults();
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        List<MovieDb> sublist = results.subList(0, Math.min(results.size(), tmdbConfig.resultLimit()));

        List<MediaDescription> descriptions = new ArrayList<>();
        for (MovieDb movieDb : sublist) {
            String posterUrl = producePosterUrl(movieDb.getPosterPath());
            String title = processTitle(movieDb.getTitle());
            LocalDate date = produceDate(movieDb.getReleaseDate());
            String description = nullIfBlank(movieDb.getOverview());
            List<String> cast = produceCast(tmdbMovies.getCredits(movieDb.getId()));

            descriptions.add(new MediaDescription( posterUrl, title, date, description, cast ));
        }

        return descriptions;
    }

    private String processTitle(String title) {
        return title.replaceAll("&", "and")
                .replaceAll(specialCharsRegex.pattern(), "");
    }

    private LocalDate produceDate(String releaseDate) {
        return TextUtils.isBlank(releaseDate) ? null : LocalDate.parse(releaseDate, DateTimeFormatter.ISO_DATE);
    }

    private String producePosterUrl(String posterPath) {
        return TextUtils.isBlank(posterPath) ? null : tmdbConfig.posterBase() + posterPath;
    }

    private String nullIfBlank(String text) {
        return TextUtils.isBlank(text) ? null : text;
    }

    private List<String> produceCast(Credits credits) {
        return credits.getCast().stream()
                .map(NamedIdElement::getName)
                .limit(tmdbConfig.resultLimit())
                .toList();
    }
}
