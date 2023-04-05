package net.cserny.rename;

import io.quarkus.test.Mock;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import static org.mockito.Mockito.mock;

@Mock
@ApplicationScoped
public class TMDBSetupMock extends TMDBSetup {

    @Produces
    public TmdbWrapper tmdbWrapper() {
        return mock(TmdbWrapper.class);
    }
}
