package net.cserny.search;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class LocalMediaSearchServiceTest {

    @Inject
    LocalMediaSearchService service;

    @BeforeEach
    public void init() {
        service.fileService.setFileSystem(Jimfs.newFileSystem(Configuration.unix()));
    }

    @Test
    @DisplayName("Check search works")
    public void checkSearch() {

    }
}
