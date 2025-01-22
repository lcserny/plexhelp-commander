package net.cserny;

import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.CachedLocalFileService;
import net.cserny.filesystem.LocalFileService;
import net.cserny.qtorrent.TorrentFile;
import net.cserny.rename.TmdbWrapper;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Slf4j
@Configuration
@EnableWebMvc
@RegisterReflectionForBinding({
        TmdbWrapper.MovieResults.class,
        TmdbWrapper.TvResults.class,
        TmdbWrapper.Movie.class,
        TmdbWrapper.Tv.class,
        TmdbWrapper.Credits.class,
        TmdbWrapper.Person.class,
        DataMapperImpl.class,
        TorrentFile.class
})
public class WebConfig implements WebMvcConfigurer {

    @Value("${http.client.connection.timeout.ms}")
    private int connectionTimeoutMs;

    @Value("${http.client.read.timeout.ms}")
    private int readTimeoutMs;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "OPTIONS", "DELETE")
                .allowedHeaders("Content-Type", "Authorization");
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(this.connectionTimeoutMs))
                .setReadTimeout(Duration.ofMillis(this.readTimeoutMs))
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "filesystem.cache", name = "enabled", havingValue = "false", matchIfMissing = true)
    public LocalFileService localFileService() {
        log.info("Using LocalFileService");
        return new LocalFileService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "filesystem.cache", name = "enabled", havingValue = "true")
    public LocalFileService cachedLocalFileService() {
        log.info("Using CachedLocalFileService");
        return new CachedLocalFileService();
    }
}
