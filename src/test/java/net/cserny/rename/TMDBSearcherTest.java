package net.cserny.rename;

import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.tv.TvSeries;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.MongoTestSetup;
import net.cserny.rename.NameNormalizer.NameYear;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@QuarkusTest
@Testcontainers
@QuarkusTestResource(MongoTestSetup.class)
class TMDBSearcherTest {

    @Inject
    TMDBSearcher searcher;

    @Inject
    OnlineCacheRepository repository;

    @Test
    @DisplayName("Check movie searched in third party API is retrieved and cached")
    void checkMovieGetAndCache() {
        int movieId = 1;
        String title = "fight club";

        MovieDb movieDb = new MovieDb();
        movieDb.setId(movieId);
        movieDb.setTitle(title);
        MovieResultsPage page = new MovieResultsPage();
        page.setResults(List.of(movieDb));

        Credits credits = new Credits();
        credits.setCast(Collections.emptyList());

        NameYear movie = new NameYear(title, 2000);
        when(searcher.tmdbSearch.searchMovie(movie.name(), movie.year(), null, false, 0))
                .thenReturn(page);
        when(searcher.tmdbMovies.getCredits(movieId))
                .thenReturn(credits);

        RenamedMediaOptions options = searcher.search(movie, MediaFileType.MOVIE);

        assertEquals(MediaRenameOrigin.TMDB, options.origin());
        assertEquals(1, options.mediaDescriptions().size());
        assertEquals(title, options.mediaDescriptions().get(0).title());

        List<OnlineCacheItem> onlineCacheItems = repository.retrieveAllByNameYearAndType(movie, MediaFileType.MOVIE);
        assertEquals(1, onlineCacheItems.size());
        assertEquals(title, onlineCacheItems.get(0).title);
    }

    @Test
    @DisplayName("Check TV show searched in third party API is retrieved and cached")
    void checkTVGetAndCache() {
        int tvId = 2;
        String title = "game of thrones";

        TvSeries tvSeries = new TvSeries();
        tvSeries.setId(tvId);
        tvSeries.setName(title);
        TvResultsPage page = new TvResultsPage();
        page.setResults(List.of(tvSeries));

        Credits credits = new Credits();
        credits.setCast(Collections.emptyList());

        NameYear tvShow = new NameYear(title, 2011);
        when(searcher.tmdbSearch.searchTv(tvShow.name(), null, 0))
                .thenReturn(page);
        when(searcher.tmdbTV.getCredits(tvId, null))
                .thenReturn(credits);

        RenamedMediaOptions options = searcher.search(tvShow, MediaFileType.TV);

        assertEquals(MediaRenameOrigin.TMDB, options.origin());
        assertEquals(1, options.mediaDescriptions().size());
        assertEquals(title, options.mediaDescriptions().get(0).title());

        List<OnlineCacheItem> onlineCacheItems = repository.retrieveAllByNameYearAndType(tvShow, MediaFileType.TV);
        assertEquals(1, onlineCacheItems.size());
        assertEquals(title, onlineCacheItems.get(0).title);
    }
}