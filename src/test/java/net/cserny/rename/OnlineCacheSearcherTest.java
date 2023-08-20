package net.cserny.rename;

import net.cserny.MongoTestConfiguration;
import net.cserny.rename.NameNormalizer.NameYear;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        OnlineCacheSearcher.class,
        OnlineCacheRepository.class
})
@DataMongoTest
@Testcontainers
public class OnlineCacheSearcherTest {

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
