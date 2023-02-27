package net.cserny.search;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.filesystem.LocalFileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.Produces;

@QuarkusTest
public class LocalMediaSearchServiceTest {

    @Inject
    LocalMediaSearchService service;

    @Produces
    public LocalFileService localFileService() {
        LocalFileService fileService = new LocalFileService();
        fileService.setFileSystem(Jimfs.newFileSystem(Configuration.unix()));
        return fileService;
    }

    @Test
    @DisplayName("Check search works")
    public void checkSearch() {

    }
}
