package net.cserny;

import lombok.extern.slf4j.Slf4j;
import net.cserny.download.DownloadedMediaRepository;
import net.cserny.filesystem.CachedLocalFileService;
import net.cserny.filesystem.FileServiceCacheLogger;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.move.*;
import net.cserny.qtorrent.TorrentFile;
import net.cserny.rename.MediaRenameService;
import net.cserny.rename.NameNormalizer;
import net.cserny.rename.TmdbWrapper;
import net.cserny.search.MediaIdentificationService;
import net.cserny.search.MediaSearchService;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.file.FileBasedStateRepository;

import java.io.File;
import java.net.URISyntaxException;
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
    FileServiceCacheLogger fileServiceCacheLogger(FeatureManager featureManager, LocalFileService fileService) {
        if (featureManager.isActive(ApplicationFeatures.FILESYSTEM_CACHE_LOGGING)) {
            log.info("Filesystem cache logging feature activated");
            return new FileServiceCacheLogger(fileService);
        }
        log.info("Filesystem cache logging feature not activated");
        return null;
    }

    @Bean
    LocalFileService fileService(FeatureManager featureManager) {
        if (featureManager.isActive(ApplicationFeatures.FILESYSTEM_CACHE)) {
            log.info("Filesystem cache feature activated");
            return new CachedLocalFileService();
        }
        log.info("Filesystem cache feature not activated");
        return new LocalFileService();
    }

    @Bean
    AutoMoveMediaService autoMoveMediaService(FeatureManager featureManager,
                                              DownloadedMediaRepository downloadedMediaRepository,
                                              AutoMoveMediaRepository autoMoveMediaRepository,
                                              MediaSearchService searchService,
                                              MediaRenameService renameService,
                                              MediaMoveService moveService,
                                              NameNormalizer normalizer,
                                              LocalFileService fileService,
                                              FilesystemProperties filesystemProperties,
                                              AutoMoveProperties properties,
                                              VirtualExecutor threadpool,
                                              MediaIdentificationService identificationService) {
        if (featureManager.isActive(ApplicationFeatures.AUTOMOVE)) {
            log.info("Automove feature activated");
            return new AutoMoveMediaService(downloadedMediaRepository, autoMoveMediaRepository,
                    searchService, renameService, moveService, normalizer, fileService,
                    filesystemProperties, properties, threadpool, identificationService);
        }
        log.info("Automove feature not activated");
        return null;
    }

    @Bean
    public FeatureManager featureManager() throws URISyntaxException {
        return new FeatureManagerBuilder()
                .stateRepository(new FileBasedStateRepository(new File(ClassLoader.getSystemResource("features.properties").toURI())))
                .featureProvider(new EnumBasedFeatureProvider(ApplicationFeatures.class))
                .build();
    }

    public enum ApplicationFeatures implements Feature {

        @EnabledByDefault
        @Label("Automatically move media files downloaded if possible")
        AUTOMOVE,

        @Label("Use a InMemory cache for the filesystem operations")
        FILESYSTEM_CACHE,

        @Label("Log operations on the InMemory cache")
        FILESYSTEM_CACHE_LOGGING;
    }
}
