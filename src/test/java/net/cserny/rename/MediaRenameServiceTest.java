package net.cserny.rename;

import net.cserny.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(TMDBSetupMock.class)
class MediaRenameServiceTest extends IntegrationTest {

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