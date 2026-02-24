package net.cserny.core.rename;

import net.cserny.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static net.cserny.BaseIntegrationTest.TestConfig.TMDBMOCK;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles(TMDBMOCK)
class MediaRenameServiceTest extends IntegrationTest {

    @Autowired
    MediaRenameService service;

    @Test
    @DisplayName("Checks that the search providers are ordered correctly")
    public void checkSearcherOrder() {
        assertEquals(DiskSearcher.class.getSimpleName(), service.getSearchers().get(0).getClass().getSimpleName());
        assertEquals(OnlineCacheSearcher.class.getSimpleName(), service.getSearchers().get(1).getClass().getSimpleName());
        assertEquals(ExternalSearcher.class.getSimpleName(), service.getSearchers().get(2).getClass().getSimpleName());
    }
}