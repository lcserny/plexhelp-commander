package net.cserny.rename;

import net.cserny.rename.NameNormalizer.NameYear;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest({
        "server.command.name=test-server",
        "server.command.listen-cron=disabled",
        "search.video-min-size-bytes=5",
        "search.exclude-paths[0]=Excluded Folder 1"
})
@ContextConfiguration(classes = {
        OnlineCacheSearcher.class,
        OnlineCacheRepository.class
})
@EnableAutoConfiguration
@EnableMongoRepositories
@Testcontainers
public class OnlineCacheSearcherTest {

    @Container
    public static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:5.0");

    @DynamicPropertySource
    public static void qTorrentProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoContainer.getConnectionString());
    }

    @Autowired
    OnlineCacheSearcher searcher;

    @Autowired
    OnlineCacheRepository repository;

    @Test
    @DisplayName("Check that cache search works correctly")
    void checkCacheSearch() {
        NameYear nameYear = new NameYear("My Movie", 2022);
        String desc = "my description";

        OnlineCacheItem item1 = new OnlineCacheItem();
        item1.setSearchName(nameYear.name());
        item1.setSearchYear(nameYear.year());
        item1.setMediaType(MediaFileType.MOVIE);
        item1.setDescription(desc);

        OnlineCacheItem item2 = new OnlineCacheItem();
        item2.setSearchName(nameYear.name());
        item2.setSearchYear(nameYear.year());
        item2.setMediaType(MediaFileType.TV);
        item2.setDescription(desc);

        repository.saveAll(List.of(item1, item2));

        RenamedMediaOptions options = searcher.search(nameYear, MediaFileType.MOVIE);

        assertEquals(1, options.mediaDescriptions().size());
        assertEquals(desc, options.mediaDescriptions().get(0).description());
    }

    @Test
    @DisplayName("Check that cache search without year works correctly")
    void checkCacheWithoutYearSearch() {
        NameYear nameYear = new NameYear("Another Movie", null);
        String desc = "another description";

        OnlineCacheItem item1 = new OnlineCacheItem();
        item1.setSearchName(nameYear.name());
        item1.setSearchYear(nameYear.year());
        item1.setMediaType(MediaFileType.MOVIE);
        item1.setDescription(desc);

        OnlineCacheItem item2 = new OnlineCacheItem();
        item2.setSearchName(nameYear.name());
        item2.setSearchYear(2022);
        item2.setMediaType(MediaFileType.MOVIE);
        item2.setDescription(desc);

        repository.saveAll(List.of(item1, item2));

        RenamedMediaOptions options = searcher.search(nameYear, MediaFileType.MOVIE);

        assertEquals(2, options.mediaDescriptions().size());
    }
}
