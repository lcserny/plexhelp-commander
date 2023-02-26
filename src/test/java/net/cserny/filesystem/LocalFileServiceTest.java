package net.cserny.filesystem;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class LocalFileServiceTest {

    @Inject
    LocalFileService service;

    @BeforeEach
    public void init() {
        service.setFileSystem(Jimfs.newFileSystem(Configuration.unix()));
    }

    @Test
    @DisplayName("Check filesystem works")
    public void checkFilesystem() {

    }
}
