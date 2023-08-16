package net.cserny.rename;

import net.cserny.rename.NameNormalizer.NameYear;
import net.cserny.rename.TmdbWrapper.Credits;
import net.cserny.rename.TmdbWrapper.Movie;
import net.cserny.rename.TmdbWrapper.Tv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest({
        "server.command.name=test-server",
        "server.command.listen-cron=disabled",
        "search.video-min-size-bytes=5",
        "search.exclude-paths[0]=Excluded Folder 1"
})
@EnableAutoConfiguration
@EnableMongoRepositories
@ContextConfiguration(classes = {
        TMDBSearcher.class,
        OnlineCacheRepository.class,
        OnlineConfig.class,
        TmdbConfig.class,
        RestTemplate.class,
        TMDBSetupMock.class
})
@Testcontainers
class TMDBSearcherTest {

    @Container
    public static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:5.0");

    @DynamicPropertySource
    public static void qTorrentProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoContainer.getConnectionString());
    }

    @Autowired
    TMDBSearcher searcher;

    @Autowired
    OnlineCacheRepository repository;

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
        when(searcher.tmdbWrapper.searchMovies(eq(movie.name()), eq(movie.year())))
                .thenReturn(results);
        when(searcher.tmdbWrapper.movieCredits(eq(movieId)))
                .thenReturn(credits);

        RenamedMediaOptions options = searcher.search(movie, MediaFileType.MOVIE);

        assertEquals(MediaRenameOrigin.TMDB, options.origin());
        assertEquals(1, options.mediaDescriptions().size());
        assertEquals(title, options.mediaDescriptions().get(0).title());

        List<OnlineCacheItem> onlineCacheItems = repository.autoRetrieveAllByNameYearAndType(movie, MediaFileType.MOVIE);
        assertEquals(1, onlineCacheItems.size());
        assertEquals(title, onlineCacheItems.get(0).title);
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
        when(searcher.tmdbWrapper.searchTvShows(eq(tvShow.name()), eq(tvShow.year())))
                .thenReturn(results);
        when(searcher.tmdbWrapper.tvShowCredits(eq(tvId)))
                .thenReturn(credits);

        RenamedMediaOptions options = searcher.search(tvShow, MediaFileType.TV);

        assertEquals(MediaRenameOrigin.TMDB, options.origin());
        assertEquals(1, options.mediaDescriptions().size());
        assertEquals(title, options.mediaDescriptions().get(0).title());

        List<OnlineCacheItem> onlineCacheItems = repository.autoRetrieveAllByNameYearAndType(tvShow, MediaFileType.TV);
        assertEquals(1, onlineCacheItems.size());
        assertEquals(title, onlineCacheItems.get(0).title);
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