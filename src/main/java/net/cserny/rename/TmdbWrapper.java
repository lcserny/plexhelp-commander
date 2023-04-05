package net.cserny.rename;

import io.v47.tmdb.TmdbClient;
import io.v47.tmdb.model.*;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

public class TmdbWrapper {

    private final TmdbClient tmdbClient;

    public TmdbWrapper(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    public Publisher<PaginatedListResults<TvListResult>> searchTvShows(String query, Integer year) {
        return tmdbClient.getSearch().forTvShows(query, 1, null, year);
    }

    public Publisher<PaginatedListResults<MovieListResult>> searchMovies(String query, Integer year) {
        return tmdbClient.getSearch().forMovies(query, 1, null, null, false, year, null);
    }

    public Publisher<MovieCredits> movieCredits(int movieId) {
        return tmdbClient.getMovie().credits(movieId);
    }

    public Publisher<TvShowCredits> tvShowCredits(int tvShowCredits) {
        return tmdbClient.getTvShow().credits(tvShowCredits, null);
    }
}
