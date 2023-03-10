package net.cserny.rename;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.TmdbTV;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class TMDBSetup {

    @Inject
    TMDBConfig config;

    @Produces
    public TmdbApi tmdbApi() {
        return new TmdbApi(config.apiKey());
    }

    @Produces
    public TmdbSearch tmdbSearch(TmdbApi tmdbApi) {
        return tmdbApi.getSearch();
    }

    @Produces
    public TmdbMovies tmdbMovies(TmdbApi tmdbApi) {
        return tmdbApi.getMovies();
    }

    @Produces
    public TmdbTV tmdbTV(TmdbApi tmdbApi) {
        return tmdbApi.getTvSeries();
    }
}
