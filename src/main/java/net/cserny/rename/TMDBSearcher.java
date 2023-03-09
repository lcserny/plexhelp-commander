package net.cserny.rename;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.TmdbTV;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.core.NamedIdElement;
import net.cserny.rename.NameNormalizer.NameYear;

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
import java.util.stream.Collectors;

@Priority(0)
@Singleton
public class TMDBSearcher implements Searcher {

    @Inject
    OnlineCacheRepository repository;

    @Inject
    TMDBConfig tmdbConfig;

    private TmdbSearch tmdbSearch;

    private static final String POSTER_BASE = "http://image.tmdb.org/t/p/w92";
    // TODO: move this in Angular resources?
    private static final String FALLBACK_POSTER_LOCAL = "/static/img/no-poster.jpg";

    private final Pattern specialCharsRegex = Pattern.compile("[^a-zA-Z0-9-\s]");

    @PostConstruct
    public void init() {
        TmdbApi tmdbApi = new TmdbApi(tmdbConfig.apiKey());
        tmdbSearch = tmdbApi.getSearch();
    }

    // TODO: do searchTV, refactor to methods this
    @Override
    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        MovieResultsPage page = tmdbSearch.searchMovie(nameYear.name(), nameYear.year(), null, false, 0);
        List<MovieDb> results = page.getResults();
        if (results == null || results.isEmpty()) {
            return new RenamedMediaOptions(MediaRenameOrigin.TMDB, Collections.emptyList());
        }

        List<MovieDb> sublist = results.subList(0, Math.min(results.size(), tmdbConfig.resultLimit()));

        List<MediaDescription> descriptions = new ArrayList<>();
        for (MovieDb movieDb : sublist) {
            String posterUrl = FALLBACK_POSTER_LOCAL;
            String posterPath = movieDb.getPosterPath();
            if (posterPath != null && posterPath.length() > 0) {
                posterUrl = POSTER_BASE + posterPath;
            }

            String title = movieDb.getTitle();
            title = title.replaceAll("&", "and");
            title = title.replaceAll(specialCharsRegex.pattern(), "");

            String releaseDate = movieDb.getReleaseDate();
            LocalDate date = LocalDate.parse(releaseDate, DateTimeFormatter.ISO_DATE);

            String description = movieDb.getOverview();

            List<String> cast = movieDb.getCredits().getCast().stream()
                    .map(NamedIdElement::getName)
                    .toList();

            descriptions.add(new MediaDescription(
                    posterUrl,
                    title,
                    date,
                    description,
                    cast
            ));
        }

        // TODO: save in cache

        return new RenamedMediaOptions(MediaRenameOrigin.TMDB, descriptions);
    }
}
