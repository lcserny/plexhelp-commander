package net.cserny;

import io.micrometer.tracing.Tracer;
import net.cserny.download.DownloadedMediaRepository;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.move.AutoMoveMediaRepository;
import net.cserny.move.AutoMoveMediaService;
import net.cserny.move.AutoMoveProperties;
import net.cserny.move.MediaMoveService;
import net.cserny.rename.MediaRenameService;
import net.cserny.rename.NameNormalizer;
import net.cserny.search.MediaIdentificationService;
import net.cserny.search.MediaSearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import org.togglz.core.manager.FeatureManager;

@Profile("test")
@Configuration
@ComponentScan({
        "net.cserny.search",
        "net.cserny.rename",
        "net.cserny.move",
        "net.cserny.filesystem",
        "net.cserny.download",
        "net.cserny.magnet",
        "net.cserny.qtorrent",
})
public class TestConfig {

    @Bean
    public Tracer tracer() {
        return Tracer.NOOP;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public LocalFileService localFileService() {
        return new LocalFileService();
    }

    @Bean
    AutoMoveMediaService autoMoveMediaService(DownloadedMediaRepository downloadedMediaRepository,
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
        return new AutoMoveMediaService(downloadedMediaRepository, autoMoveMediaRepository,
                searchService, renameService, moveService, normalizer, fileService,
                filesystemProperties, properties, threadpool, identificationService);
    }
}
