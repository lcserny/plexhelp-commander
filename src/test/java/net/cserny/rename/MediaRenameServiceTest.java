package net.cserny.rename;

import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest({
        "server.command.name=test-server",
        "server.command.listen-cron=disabled",
        "search.video-min-size-bytes=5",
        "search.exclude-paths[0]=Excluded Folder 1"
})
@ContextConfiguration(classes = {
        MediaRenameService.class,
        DiskSearcher.class,
        OnlineCacheSearcher.class,
        TMDBSearcher.class,
        NameNormalizer.class,
        FilesystemConfig.class,
        RenameConfig.class,
        OnlineConfig.class,
        TMDBSetupMock.class,
        LocalFileService.class}
)
@EnableAutoConfiguration
@EnableMongoRepositories
@Testcontainers
class MediaRenameServiceTest {

    @Container
    public static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:5.0");

    @DynamicPropertySource
    public static void qTorrentProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoContainer.getConnectionString());
    }

    @Autowired
    MediaRenameService service;

    @Test
    @DisplayName("Checks that the search providers are ordered correctly")
    public void checkSearcherOrder() {
        assertEquals(DiskSearcher.class.getSimpleName(), service.searchers.get(0).getClass().getSimpleName());
        assertEquals(OnlineCacheSearcher.class.getSimpleName(), service.searchers.get(1).getClass().getSimpleName());
        assertEquals(TMDBSearcher.class.getSimpleName(), service.searchers.get(2).getClass().getSimpleName());
    }
}