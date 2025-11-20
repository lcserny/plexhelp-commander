package net.cserny.rename.internal;

import net.cserny.MongoTestConfiguration;
import net.cserny.generated.MediaFileType;
import net.cserny.rename.OnlineCacheItem;
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
    @DisplayName("Saving cache items with the same name, year and type should not create duplicates")
    void savesAreBusinessUnique() {
        String name = "Some Movie";
        Integer year = 2020;
        MediaFileType type = MediaFileType.MOVIE;

        OnlineCacheItem item1 = OnlineCacheItem.builder()
                .searchName(name)
                .searchYear(year)
                .mediaType(type)
                .title("Some Movie Title")
                .build();
        repository.save(item1);

        OnlineCacheItem item2 = OnlineCacheItem.builder()
                .searchName(name)
                .searchYear(year)
                .mediaType(type)
                .title("Some Other Movie Title")
                .build();
        repository.save(item2);

        List<OnlineCacheItem> items = repository.findByNameTypeAndOptionalYear(name, year, type);
        assertEquals(1, items.size(), "There should be only one unique item in the repository");
    }
}
