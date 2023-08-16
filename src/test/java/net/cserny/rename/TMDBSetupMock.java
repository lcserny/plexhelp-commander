package net.cserny.rename;

import io.v47.tmdb.TmdbClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@Configuration
public class TMDBSetupMock {

    @Bean
    @Primary
    public TmdbWrapper tmdbWrapper() {
        return mock(TmdbWrapper.class);
    }

    @Bean
    @Primary
    public TmdbClient tmdbClient() {
        return mock(TmdbClient.class);
    }
}
