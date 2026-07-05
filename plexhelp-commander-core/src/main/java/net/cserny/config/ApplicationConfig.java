package net.cserny.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.QBitTorrentRestApi;
import net.cserny.core.command.CommandRunner;
import net.cserny.core.command.NativeCommandRunner;
import net.cserny.core.command.SshCommandRunner;
import net.cserny.core.rename.tmdb.TmdbRestApi;
import net.cserny.support.Features;
import net.cserny.support.UtilityProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.support.RestTemplateAdapter;
import org.springframework.web.service.invoker.HttpExchangeAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.mongodb.MongoStateRepository;
import org.togglz.spring.boot.actuate.autoconfigure.TogglzProperties;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@ComponentScan(basePackages = {"net.cserny"})
@EnableAsync
@EnableMongoRepositories(basePackages = "net.cserny")
@EnableScheduling
@EnableMongoAuditing
@EnableConfigurationProperties({TogglzProperties.class, TorrentProperties.class})
public class ApplicationConfig {

    public static final int MAX_SUBS_ALLOWED = 5;

    @Bean
    @ConditionalOnMissingBean
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
        return new NativeCommandRunner(executorService);
    }

    @Bean
    public QBitTorrentRestApi qBitTorrentRestApi(TorrentProperties torrentProperties) {
        HttpExchangeAdapter adapter = RestTemplateAdapter.create(new RestTemplateBuilder()
                .uriTemplateHandler(new DefaultUriBuilderFactory(torrentProperties.getBaseUrl()))
                .connectTimeout(Duration.ofMillis(torrentProperties.getConnectionTimeout()))
                .readTimeout(Duration.ofMillis(torrentProperties.getReadTimeout()))
                .build());

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(QBitTorrentRestApi.class);
    }

    @Bean
    public TmdbRestApi tmdbRestApi(TmdbProperties tmdbProperties) {
        DefaultUriBuilderFactory urlFactory = new DefaultUriBuilderFactory(tmdbProperties.getBaseUrl());
        urlFactory.setDefaultUriVariables(Map.of("tmdbApiKey", tmdbProperties.getApiKey()));

        HttpExchangeAdapter adapter = RestTemplateAdapter.create(new RestTemplateBuilder()
                .uriTemplateHandler(urlFactory)
                .connectTimeout(Duration.ofMillis(tmdbProperties.getConnectionTimeout()))
                .readTimeout(Duration.ofMillis(tmdbProperties.getReadTimeout()))
                .build());

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(TmdbRestApi.class);
    }


    @Bean
    public StateRepository stateRepository(MongoClient mongoClient,
                                           MongoProperties mongoProperties,
                                           TogglzProperties togglzProperties) {
        // default collection name is "togglz"
        StateRepository mongoRepo = MongoStateRepository
                .newBuilder(mongoClient, mongoProperties.getDatabase())
                .build();
        return new CachingStateRepository(mongoRepo, togglzProperties.getCache().getTimeToLive());
    }

    @Bean
    public FeatureProvider featureProvider() {
        return new EnumBasedFeatureProvider().addFeatureEnum(Features.class);
    }
}
