package net.cserny.rename;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.TmdbTV;
import io.quarkus.test.Mock;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import static org.mockito.Mockito.mock;

@Mock
@ApplicationScoped
public class TMDBSetupMock extends TMDBSetup {

    @Produces
    public TmdbApi tmdbApi() {
        return null;
    }

    @Produces
    public TmdbSearch tmdbSearch(TmdbApi tmdbApi) {
        return mock(TmdbSearch.class);
    }

    @Produces
    public TmdbMovies tmdbMovies(TmdbApi tmdbApi) {
        return mock(TmdbMovies.class);
    }

    @Produces
    public TmdbTV tmdbTV(TmdbApi tmdbApi) {
        return mock(TmdbTV.class);
    }
}
