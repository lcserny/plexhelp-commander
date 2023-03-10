package net.cserny.rename;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.MongoTestSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Testcontainers
@QuarkusTestResource(MongoTestSetup.class)
class MediaRenameServiceTest {

    @Inject
    MediaRenameService service;

    @Test
    @DisplayName("Checks that the search providers are ordered correctly")
    public void checkSearcherOrder() {
        assertEquals(DiskSearcher.class.getSimpleName(), service.searchers.get(0).getClass().getSimpleName());
        assertEquals(OnlineCacheSearcher.class.getSimpleName(), service.searchers.get(1).getClass().getSimpleName());
        assertEquals(TMDBSearcher.class.getSimpleName(), service.searchers.get(2).getClass().getSimpleName());
    }
}