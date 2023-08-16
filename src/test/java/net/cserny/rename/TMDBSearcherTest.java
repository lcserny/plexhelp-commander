package net.cserny.rename;

import io.smallrye.mutiny.operators.multi.builders.CollectionBasedMulti;
import io.v47.tmdb.model.*;
import net.cserny.rename.NameNormalizer.NameYear;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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

        MovieListResult movieDb = createMovie(movieId, title);
        PaginatedListResults<MovieListResult> page = new PaginatedListResults<>(null, 0, List.of(movieDb), 1, 1);
        MovieCredits credits = new MovieCredits(null, Collections.emptyList(), Collections.emptyList());

        NameYear movie = new NameYear(title, 2000);
        when(searcher.tmdbWrapper.searchMovies(eq(movie.name()), eq(movie.year())))
                .thenReturn(new CollectionBasedMulti<>(page));
        when(searcher.tmdbWrapper.movieCredits(eq(movieId)))
                .thenReturn(new CollectionBasedMulti<>(credits));

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

        TvListResult tvSeries = createTvShow(tvId, title);
        PaginatedListResults<TvListResult> page = new PaginatedListResults<>(null, 0, List.of(tvSeries), 1, 1);
        TvShowCredits credits = new TvShowCredits(null, Collections.emptyList(), Collections.emptyList());

        NameYear tvShow = new NameYear(title, 2011);
        when(searcher.tmdbWrapper.searchTvShows(eq(tvShow.name()), eq(tvShow.year())))
                .thenReturn(new CollectionBasedMulti<>(page));
        when(searcher.tmdbWrapper.tvShowCredits(eq(tvId)))
                .thenReturn(new CollectionBasedMulti<>(credits));

        RenamedMediaOptions options = searcher.search(tvShow, MediaFileType.TV);

        assertEquals(MediaRenameOrigin.TMDB, options.origin());
        assertEquals(1, options.mediaDescriptions().size());
        assertEquals(title, options.mediaDescriptions().get(0).title());

        List<OnlineCacheItem> onlineCacheItems = repository.autoRetrieveAllByNameYearAndType(tvShow, MediaFileType.TV);
        assertEquals(1, onlineCacheItems.size());
        assertEquals(title, onlineCacheItems.get(0).title);
    }

    private MovieListResult createMovie(int id, String title) {
        return new MovieListResult( null, false, null,
                null, Collections.emptyList(), id, null,
                null, title, null, null,
                null, null, null, null, MediaType.Movie
        );
    }

    private TvListResult createTvShow(int id, String title) {
        return new TvListResult( null, null, id, null,
                null, null, null, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), null, null,
                null, title, null, MediaType.Tv, false
        );
    }
}