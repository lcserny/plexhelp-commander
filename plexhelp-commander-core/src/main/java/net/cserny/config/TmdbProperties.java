package net.cserny.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tmdb.client")
public class TmdbProperties {

    private String apiKey;
    private String baseUrl;
    private int readTimeout;
}
