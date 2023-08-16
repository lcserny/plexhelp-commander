package net.cserny.rename;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Flow;

@Component
public class TmdbWrapper {

    @Autowired
    TmdbApi tmdbApi;

    public TvResultsPage searchTvShows(String query) {
        return tmdbApi.getSearch().searchTv(query, null, 0);
    }

    public MovieResultsPage searchMovies(String query, Integer year) {
        return tmdbApi.getSearch().searchMovie(query, year, null, false, 0);
    }

    public Credits movieCredits(int movieId) {
        return tmdbApi.getMovies().getCredits(movieId);
    }

    public Credits tvShowCredits(int tvShowCredits) {
        return tmdbApi.getTvSeries().getCredits(tvShowCredits, null);
    }
}
