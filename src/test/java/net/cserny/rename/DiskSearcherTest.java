package net.cserny.rename;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.filesystem.LocalFileService;
import org.junit.jupiter.api.BeforeEach;

import javax.inject.Inject;

@QuarkusTest
public class DiskSearcherTest {

    @Inject
    DiskSearcher searcher;

    @Inject
    LocalFileService fileService;

    @BeforeEach
    public void setup() {
        fileService.fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    // TODO
}
