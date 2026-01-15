package net.cserny.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.extern.slf4j.Slf4j;
import net.cserny.command.OsExecutor;
import net.cserny.command.ProcessExecutor;
import net.cserny.command.ServerCommandProperties;
import net.cserny.command.SshExecutor;
import net.cserny.qtorrent.TorrentFile;
import net.cserny.rename.TmdbWrapper;
import net.cserny.support.DataMapperImpl;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@EnableWebMvc
@EnableMongoRepositories(basePackages = "net.cserny")
@EnableScheduling
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
public class ApplicationConfiguration implements WebMvcConfigurer {

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
                .connectTimeout(Duration.ofMillis(this.connectionTimeoutMs))
                .readTimeout(Duration.ofMillis(this.readTimeoutMs))
                .build();
    }

    @Bean
    FileSystem fileSystem() {
        return FileSystems.getDefault();
    }

    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    @Bean
    public ExecutorService tracedExecutor() {
        ExecutorService delegate = Executors.newVirtualThreadPerTaskExecutor();
        return ContextExecutorService.wrap(delegate, ContextSnapshotFactory.builder().build());
    }

    @Bean
    public OsExecutor osExecutor(ServerCommandProperties serverCommandProperties, ExecutorService executorService) {
        if (serverCommandProperties.getSsh().isEnabled()) {
            return new SshExecutor(serverCommandProperties);
        }
        return new ProcessExecutor(executorService);
    }

    // TODO
    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(1000); // Truncate large bodies
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }
}
