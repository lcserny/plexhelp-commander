package net.cserny.rename;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tmdb.client")
public class TmdbProperties {

    private String apiKey;
    private String baseUrl;
    private String searchMoviesUrl;
    private String movieCreditsUrl;
    private String searchTvUrl;
    private String tvCreditsUrl;
}
