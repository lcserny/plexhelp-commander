package net.cserny.rename;

import io.v47.tmdb.TmdbClient;
import io.v47.tmdb.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Flow;

@Component
public class TmdbWrapper {

    @Autowired
    TmdbClient tmdbClient;

    public Flow.Publisher<PaginatedListResults<TvListResult>> searchTvShows(String query, Integer year) {
        return tmdbClient.getSearch().forTvShows(query, 1, null, year);
    }

    public Flow.Publisher<PaginatedListResults<MovieListResult>> searchMovies(String query, Integer year) {
        return tmdbClient.getSearch().forMovies(query, 1, null, null, false, year, null);
    }

    public Flow.Publisher<MovieCredits> movieCredits(int movieId) {
        return tmdbClient.getMovie().credits(movieId);
    }

    public Flow.Publisher<TvShowCredits> tvShowCredits(int tvShowCredits) {
        return tmdbClient.getTvShow().credits(tvShowCredits, null);
    }
}
