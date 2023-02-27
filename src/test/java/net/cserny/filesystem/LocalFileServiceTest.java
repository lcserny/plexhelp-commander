package net.cserny.filesystem;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class LocalFileServiceTest {

    @Inject
    LocalFileService service;

    @BeforeEach
    public void init() {
        service.setFileSystem(Jimfs.newFileSystem(Configuration.unix()));
    }

    @Test
    @DisplayName("Service can delete a path")
    public void deleteWorks() throws IOException {
        LocalPath localPath = createFile("/hello/my.txt");

        assertTrue(Files.exists(localPath.path()));

        service.delete(localPath);

        assertFalse(Files.exists(localPath.path()));
    }

    @Test
    @DisplayName("Service can move a path")
    public void moveWorks() throws IOException {
        LocalPath localPath = createFile("/hmmm/test.txt");
        LocalPath destPath = service.produceLocalPath("/someOther/path/text.txt");

        assertTrue(Files.exists(localPath.path()));
        assertFalse(Files.exists(destPath.path()));

        service.move(localPath, destPath);

        assertFalse(Files.exists(localPath.path()));
        assertTrue(Files.exists(destPath.path()));
    }

    @Test
    @DisplayName("Service can walk a path")
    public void walkWorks() throws IOException {
        LocalPath rootPath = service.produceLocalPath("/mypath");
        String path1 = "/mypath/file1.txt";
        String path2 = "/mypath/file2.txt";
        String path3 = "/mypath/sub1/file3.txt";
        String path4 = "/mypath/file4.txt";

        createFile(path1);
        createFile(path2);
        createFile(path3);
        createFile(path4);

        List<Path> filesFound = service.walk(rootPath, 1);

        assertEquals(3, filesFound.size());
        assertEquals(path1, filesFound.get(0).toAbsolutePath().toString());
        assertEquals(path2, filesFound.get(1).toAbsolutePath().toString());
        assertEquals(path4, filesFound.get(2).toAbsolutePath().toString());
    }

    private LocalPath createFile(String somePath) throws IOException {
        LocalPath localPath = service.produceLocalPath(somePath);
        if (localPath.path().getParent() != null) {
            Files.createDirectories(localPath.path().getParent());
        }
        Files.createFile(localPath.path());
        return localPath;
    }
}
