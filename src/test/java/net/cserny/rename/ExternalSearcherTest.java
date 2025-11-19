package net.cserny.rename;

import net.cserny.MongoTestConfiguration;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaRenameOrigin;
import net.cserny.generated.RenamedMediaOptions;
import net.cserny.rename.NameNormalizer.NameYear;
import net.cserny.rename.TmdbWrapper.Credits;
import net.cserny.rename.TmdbWrapper.Movie;
import net.cserny.rename.TmdbWrapper.Tv;
import net.cserny.rename.internal.OnlineCacheRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        ExternalSearcher.class,
        MongoTestConfiguration.class,
        OnlineCacheRepository.class,
        OnlineProperties.class,
        TmdbProperties.class,
        RestTemplate.class,
        TMDBSetupMock.class
})
@DataMongoTest
@Testcontainers
class ExternalSearcherTest {

    @Autowired
    ExternalSearcher searcher;

    @Autowired
    OnlineCacheRepository repository;

    @Autowired
    TmdbWrapper tmdbWrapper;

    @Test
    @DisplayName("Check movie searched in third party API is retrieved and cached")
    void checkMovieGetAndCache() {
        int movieId = 1;
        String title = "fight club";

        Movie movieDb = createMovie(movieId, title);
        List<Movie> results = new ArrayList<>();
        results.add(movieDb);
        Credits credits = new Credits();
        credits.setCast(Collections.emptyList());

        NameYear movie = new NameYear(title, 2000);
        when(tmdbWrapper.searchMovies(eq(movie.name()), eq(movie.year())))
                .thenReturn(results);
        when(tmdbWrapper.movieCredits(eq(movieId)))
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
        Credits credits = new Credits();
        credits.setCast(Collections.emptyList());

        NameYear tvShow = new NameYear(title, 2011);
        when(tmdbWrapper.searchTvShows(eq(tvShow.name()), eq(tvShow.year())))
                .thenReturn(results);
        when(tmdbWrapper.tvShowCredits(eq(tvId)))
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