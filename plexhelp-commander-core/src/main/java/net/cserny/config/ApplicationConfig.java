package net.cserny.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.extern.slf4j.Slf4j;
import net.cserny.core.command.CommandRunner;
import net.cserny.core.command.NativeCommandRunner;
import net.cserny.core.command.SshCommandRunner;
import net.cserny.core.rename.tmdb.TmdbRestApi;
import net.cserny.core.torrent.qbittorrent.QBitTorrentRestApi;
import net.cserny.core.torrent.qbittorrent.QBitTorrentSidInterceptor;
import net.cserny.support.Features;
import net.cserny.support.UtilityProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
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

import static net.cserny.core.rename.tmdb.TmdbRestApi.Routes.API_KEY_PARAM;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Slf4j
@Configuration
@ComponentScan(basePackages = {"net.cserny"})
@EnableAsync
@EnableMongoRepositories(basePackages = "net.cserny")
@EnableScheduling
@EnableMongoAuditing
@EnableConfigurationProperties({
        TogglzProperties.class,
        TorrentProperties.class,
        AutoMoveProperties.class,
        FilesystemProperties.class,
        MoveProperties.class,
        OnlineProperties.class,
        RenameProperties.class,
        SearchProperties.class,
        ServerCommandProperties.class,
        TmdbProperties.class
})
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
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofMillis(torrentProperties.getReadTimeout()));

        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl(torrentProperties.getBaseUrl())
                .requestFactory(new BufferingClientHttpRequestFactory(requestFactory))
                .defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);

        ClientHttpRequestInterceptor sidInterceptor = new QBitTorrentSidInterceptor(torrentProperties, restClientBuilder.clone().build());

        HttpExchangeAdapter adapter = RestClientAdapter.create(restClientBuilder
                .requestInterceptor(sidInterceptor)
                .build());

        return HttpServiceProxyFactory.builderFor(adapter).build().createClient(QBitTorrentRestApi.class);
    }

    @Bean
    public TmdbRestApi tmdbRestApi(TmdbProperties tmdbProperties) {
        DefaultUriBuilderFactory urlFactory = new DefaultUriBuilderFactory(tmdbProperties.getBaseUrl());
        urlFactory.setDefaultUriVariables(Map.of(API_KEY_PARAM, tmdbProperties.getApiKey()));

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofMillis(tmdbProperties.getReadTimeout()));

        HttpExchangeAdapter adapter = RestClientAdapter.create(RestClient.builder()
                .uriBuilderFactory(urlFactory)
                .requestFactory(requestFactory)
                .build());

        return HttpServiceProxyFactory.builderFor(adapter).build().createClient(TmdbRestApi.class);
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
