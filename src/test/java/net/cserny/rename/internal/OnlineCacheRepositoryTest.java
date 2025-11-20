package net.cserny.rename.internal;

import net.cserny.MongoTestConfiguration;
import net.cserny.generated.MediaFileType;
import net.cserny.rename.OnlineCacheItem;
import org.bson.types.ObjectId;
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
        OnlineCacheRepository.class
})
@DataMongoTest
@Testcontainers
public class OnlineCacheRepositoryTest {

    @Autowired
    OnlineCacheRepository repository;

    @Test
    @DisplayName("Saving cache items with the same details (except id) should not create duplicates")
    void savesAreBusinessUnique() {
        String name = "Some Movie";
        Integer year = 2020;
        MediaFileType type = MediaFileType.MOVIE;
        String someMovieTitle = "Some Movie Title";

        OnlineCacheItem item1 = OnlineCacheItem.builder()
                .id(new ObjectId("64b64c4f2f4d2c6f8e8e8e8e"))
                .searchName(name)
                .searchYear(year)
                .mediaType(type)
                .title(someMovieTitle)
                .build();
        repository.save(item1);

        OnlineCacheItem item2 = OnlineCacheItem.builder()
                .id(new ObjectId("64b64c4f2f4d2c6f8e8e8e8f"))
                .searchName(name)
                .searchYear(year)
                .mediaType(type)
                .title(someMovieTitle)
                .build();
        repository.save(item2);

        List<OnlineCacheItem> items = repository.findByNameTypeAndOptionalYear(name, year, type);
        assertEquals(1, items.size(), "There should be only one unique item in the repository");
    }

    @Test
    @DisplayName("Saving cache items with different details (except id) should create new entries")
    void savesWithDifferentDetailsProperly() {
        String name = "hehe Movie";
        Integer year = 2023;
        MediaFileType type = MediaFileType.MOVIE;

        OnlineCacheItem item1 = OnlineCacheItem.builder()
                .id(new ObjectId("64b64c4f2f4d2c6f8e8e8e8e"))
                .searchName(name)
                .searchYear(year)
                .mediaType(type)
                .title("AAAAAA Some Movie Title")
                .build();
        repository.save(item1);

        OnlineCacheItem item2 = OnlineCacheItem.builder()
                .id(new ObjectId("64b64c4f2f4d2c6f8e8e8e8f"))
                .searchName(name)
                .searchYear(year)
                .mediaType(type)
                .title("Another Movie Title")
                .build();
        repository.save(item2);

        List<OnlineCacheItem> items = repository.findByNameTypeAndOptionalYear(name, year, type);
        assertEquals(2, items.size(), "There should be two items in the repository");
    }
}
