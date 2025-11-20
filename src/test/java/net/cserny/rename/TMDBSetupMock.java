package net.cserny.rename;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TMDBSetupMock {

    @Bean
    @Primary
    public TmdbWrapper tmdbWrapper() {
        return mock(TmdbWrapper.class);
    }
}
