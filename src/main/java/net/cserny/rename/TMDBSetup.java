package net.cserny.rename;

import io.v47.tmdb.TmdbClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class TMDBSetup {

    @Inject
    TmdbClient tmdbClient;

    @Produces
    public TmdbWrapper tmdbWrapper() {
        return new TmdbWrapper(tmdbClient);
    }
}
