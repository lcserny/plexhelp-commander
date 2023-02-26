package net.cserny.filesystem;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class LocalFileServiceTest {

    LocalFileService service;

    @BeforeEach
    public void init() {
        service = new LocalFileService(Jimfs.newFileSystem(Configuration.unix()));
    }

    @Test
    @DisplayName("Check filesystem works")
    public void checkFilesystem() {

    }
}
