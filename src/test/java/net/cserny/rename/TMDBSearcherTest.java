package net.cserny.rename;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.operators.multi.builders.CollectionBasedMulti;
import io.v47.tmdb.model.*;
import net.cserny.MongoTestSetup;
import net.cserny.rename.NameNormalizer.NameYear;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
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

        List<OnlineCacheItem> onlineCacheItems = repository.retrieveAllByNameYearAndType(movie, MediaFileType.MOVIE);
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

        List<OnlineCacheItem> onlineCacheItems = repository.retrieveAllByNameYearAndType(tvShow, MediaFileType.TV);
        assertEquals(1, onlineCacheItems.size());
        assertEquals(title, onlineCacheItems.get(0).title);
    }

    private MovieListResult createMovie(int id, String title) {
        return new MovieListResult( null, false, null,
                null, Collections.emptyList(), id, null,
                null, title, null, null,
                null, null, null, null
        );
    }

    private TvListResult createTvShow(int id, String title) {
        return new TvListResult( null, null, id, null,
                null, null, null, null,
                null, null, null, null,
                title, null, null, false
        );
    }
}