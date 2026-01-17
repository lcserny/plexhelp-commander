package net.cserny.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.extern.slf4j.Slf4j;
import net.cserny.command.CommandRunner;
import net.cserny.command.CommandCommandRunner;
import net.cserny.command.ServerCommandProperties;
import net.cserny.command.SshCommandRunner;
import net.cserny.qtorrent.TorrentFile;
import net.cserny.rename.TmdbWrapper;
import net.cserny.support.CommanderController;
import net.cserny.support.DataMapperImpl;
import net.cserny.support.Features;
import net.cserny.support.UtilityProvider;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.togglz.core.Feature;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.mongodb.MongoStateRepository;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzProperties;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.cserny.support.CommanderController.BASE_PATH;

@Slf4j
@Configuration
@EnableWebMvc
@EnableMongoRepositories(basePackages = "net.cserny")
@EnableScheduling
@EnableMongoAuditing
@EnableConfigurationProperties(TogglzProperties.class)
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

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(BASE_PATH, HandlerTypePredicate.forAnnotation(CommanderController.class));
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .connectTimeout(Duration.ofMillis(this.connectionTimeoutMs))
                .readTimeout(Duration.ofMillis(this.readTimeoutMs))
                .build();
    }

    @Bean
    public StateRepository stateRepository(MongoClient mongoClient,
                                           MongoProperties mongoProperties,
                                           TogglzProperties togglzProperties) {
        StateRepository mongoRepo = MongoStateRepository
                .newBuilder(mongoClient, mongoProperties.getDatabase())
                .build();
        return new CachingStateRepository(mongoRepo, togglzProperties.getCache().getTimeToLive());
    }

    @Bean
    public FeatureProvider featureProvider() {
        return new EnumBasedFeatureProvider().addFeatureEnum(Features.class);
    }

    @Bean
    FileSystem fileSystem() {
        return FileSystems.getDefault();
    }

    @Bean
    ObjectMapper objectMapper() {
        return UtilityProvider.MAPPER;
    }

    @Bean
    public ExecutorService tracedExecutor() {
        ExecutorService delegate = Executors.newVirtualThreadPerTaskExecutor();
        return ContextExecutorService.wrap(delegate, ContextSnapshotFactory.builder().build());
    }

    @Bean
    public CommandRunner osExecutor(ServerCommandProperties serverCommandProperties, ExecutorService executorService) {
        if (serverCommandProperties.getSsh().isEnabled()) {
            return new SshCommandRunner(serverCommandProperties);
        }
        return new CommandCommandRunner(executorService);
    }
}
