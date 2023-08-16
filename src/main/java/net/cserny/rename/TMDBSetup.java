package net.cserny.rename;

import info.movito.themoviedbapi.TmdbApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TMDBSetup {

    @Value("${tmdb.client.api-key}")
    private String apiKey;

    @Bean
    public TmdbApi tmdbApi() {
        return new TmdbApi(apiKey);
    }
}
