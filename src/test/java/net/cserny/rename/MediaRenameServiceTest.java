package net.cserny.rename;

import net.cserny.MongoTestConfiguration;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {
        MediaRenameService.class,
        MongoTestConfiguration.class,
        DiskSearcher.class,
        OnlineCacheSearcher.class,
        ExternalSearcher.class,
        NameNormalizer.class,
        FilesystemProperties.class,
        RenameConfig.class,
        OnlineProperties.class,
        TmdbProperties.class,
        RestTemplate.class,
        TMDBSetupMock.class,
        LocalFileService.class}
)
@DataMongoTest
@Testcontainers
class MediaRenameServiceTest {

    @Autowired
    MediaRenameService service;

    @Test
    @DisplayName("Checks that the search providers are ordered correctly")
    public void checkSearcherOrder() {
        assertEquals(DiskSearcher.class.getSimpleName(), service.searchers.get(0).getClass().getSimpleName());
        assertEquals(OnlineCacheSearcher.class.getSimpleName(), service.searchers.get(1).getClass().getSimpleName());
        assertEquals(ExternalSearcher.class.getSimpleName(), service.searchers.get(2).getClass().getSimpleName());
    }
}