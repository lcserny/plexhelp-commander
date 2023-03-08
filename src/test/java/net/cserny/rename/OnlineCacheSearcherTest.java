package net.cserny.rename;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.MongoTestSetup;
import net.cserny.rename.NameNormalizer.NameYear;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
@Testcontainers
@QuarkusTestResource(MongoTestSetup.class)
public class OnlineCacheSearcherTest {

    @Inject
    OnlineCacheSearcher searcher;

    @Inject
    OnlineCacheRepository repository;

    @Test
    @DisplayName("Check that cache search works correctly")
    void checkCacheSearch() {
        NameYear nameYear = new NameYear("My Movie", 2022);
        String desc = "my description";

        OnlineCacheItem item1 = new OnlineCacheItem();
        item1.searchName = nameYear.name();
        item1.searchYear = nameYear.year();
        item1.mediaType = MediaFileType.MOVIE;
        item1.description = desc;

        OnlineCacheItem item2 = new OnlineCacheItem();
        item2.searchName = nameYear.name();
        item2.searchYear = nameYear.year();
        item2.mediaType = MediaFileType.TV;
        item2.description = desc;

        repository.persist(List.of(item1, item2));

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
        item1.searchName = nameYear.name();
        item1.searchYear = nameYear.year();
        item1.mediaType = MediaFileType.MOVIE;
        item1.description = desc;

        OnlineCacheItem item2 = new OnlineCacheItem();
        item2.searchName = nameYear.name();
        item2.searchYear = 2022;
        item2.mediaType = MediaFileType.MOVIE;
        item2.description = desc;

        repository.persist(List.of(item1, item2));

        RenamedMediaOptions options = searcher.search(nameYear, MediaFileType.MOVIE);

        assertEquals(2, options.mediaDescriptions().size());
    }
}
