package net.cserny.core.rename;

import net.cserny.IntegrationTest;
import net.cserny.api.NameNormalizer.NameYear;
import net.cserny.core.rename.internal.OnlineCacheRepository;
import net.cserny.core.rename.tmdb.TmdbRestApi;
import net.cserny.core.rename.tmdb.TmdbRestApi.*;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaRenameOrigin;
import net.cserny.generated.RenamedMediaOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ExternalRenameSearcherTest extends IntegrationTest {

    @Autowired
    ExternalRenameSearcher searcher;

    @Autowired
    OnlineCacheRepository repository;

    @MockitoBean
    TmdbRestApi tmdbRestClient;

    @Test
    @DisplayName("Check movie searched in third party API is retrieved and cached")
    void checkMovieGetAndCache() {
        int movieId = 1;
        String title = "fight club";

        Movie movieDb = createMovie(movieId, title);
        List<Movie> results = new ArrayList<>();
        results.add(movieDb);
        MovieResults movies = new MovieResults();
        movies.setResults(results);
        Credits credits = new Credits();
        credits.setCast(Collections.emptyList());

        NameYear movie = new NameYear(title, 2000);
        when(tmdbRestClient.searchMovies(eq(movie.name()), eq(movie.year())))
                .thenReturn(movies);
        when(tmdbRestClient.movieCredits(eq(movieId)))
                .thenReturn(credits);

        RenamedMediaOptions options = searcher.search(movie, MediaFileType.MOVIE);

        assertEquals(MediaRenameOrigin.EXTERNAL, options.getOrigin());
        assertEquals(1, options.getMediaDescriptions().size());
        assertEquals(title, options.getMediaDescriptions().getFirst().getTitle());

        List<OnlineCacheItem> onlineCacheItems = repository.findByNameTypeAndOptionalYear(movie, MediaFileType.MOVIE);
        assertEquals(1, onlineCacheItems.size());
        assertEquals(title, onlineCacheItems.getFirst().getTitle());
    }

    @Test
    @DisplayName("Check TV show searched in third party API is retrieved and cached")
    void checkTVGetAndCache() {
        int tvId = 2;
        String title = "game of thrones";

        Tv tvSeries = createTvShow(tvId, title);
        List<Tv> results = new ArrayList<>();
        results.add(tvSeries);
        TvResults tvShows = new TvResults();
        tvShows.setResults(results);
        Credits credits = new Credits();
        credits.setCast(Collections.emptyList());

        NameYear tvShow = new NameYear(title, 2011);
        when(tmdbRestClient.searchTvShows(eq(tvShow.name()), eq(tvShow.year())))
                .thenReturn(tvShows);
        when(tmdbRestClient.tvShowCredits(eq(tvId)))
                .thenReturn(credits);

        RenamedMediaOptions options = searcher.search(tvShow, MediaFileType.TV);

        assertEquals(MediaRenameOrigin.EXTERNAL, options.getOrigin());
        assertEquals(1, options.getMediaDescriptions().size());
        assertEquals(title, options.getMediaDescriptions().getFirst().getTitle());

        List<OnlineCacheItem> onlineCacheItems = repository.findByNameTypeAndOptionalYear(tvShow, MediaFileType.TV);
        assertEquals(1, onlineCacheItems.size());
        assertEquals(title, onlineCacheItems.getFirst().getTitle());
    }

    private Movie createMovie(int id, String title) {
        Movie movie =  new Movie();
        movie.setId(id);
        movie.setTitle(title);
        return movie;
    }

    private Tv createTvShow(int id, String title) {
        Tv series = new Tv();
        series.setId(id);
        series.setName(title);
        return series;
    }
}