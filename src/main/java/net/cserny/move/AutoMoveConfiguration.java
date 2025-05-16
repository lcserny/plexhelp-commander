package net.cserny.move;

import lombok.extern.slf4j.Slf4j;
import net.cserny.VirtualExecutor;
import net.cserny.download.DownloadedMediaRepository;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.rename.MediaRenameService;
import net.cserny.rename.NameNormalizer;
import net.cserny.search.MediaIdentificationService;
import net.cserny.search.MediaSearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.manager.*;
import org.togglz.core.repository.file.FileBasedStateRepository;
import org.togglz.core.spi.FeatureManagerProvider;
import org.togglz.core.spi.FeatureProvider;

import java.io.File;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class AutoMoveConfiguration {

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
        if (featureManager.isActive(MyFeatures.AUTOMOVE)) {
            log.info("Automove feature activated");
            return new AutoMoveMediaService(downloadedMediaRepository, autoMoveMediaRepository,
                    searchService, renameService, moveService, normalizer, fileService,
                    filesystemProperties, properties, threadpool, identificationService);
        }
        log.info("Automove feature not activated");
        return null;
    }

    @Bean
    public FeatureManager featureManager() {
        return new FeatureManagerBuilder()
                .stateRepository(new FileBasedStateRepository(getResourceFile("togglz.properties")))
                .featureProvider(new EnumBasedFeatureProvider(MyFeatures.class))
                .build();
    }

    private File getResourceFile(String resourcePath) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource(resourcePath);
            if (url == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            return new File(url.toURI());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource: " + resourcePath, e);
        }
    }

    public enum MyFeatures implements Feature {

        @Label("First Feature")
        AUTOMOVE,
    }
}
