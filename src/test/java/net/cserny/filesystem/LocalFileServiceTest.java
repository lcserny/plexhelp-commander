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
import java.nio.file.NotDirectoryException;
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

    @Test
    @DisplayName("Service can walk a path of max depth passed from path passed")
    public void walkDepthIsCorrect() throws IOException {
        LocalPath rootPath = service.produceLocalPath("/mypath/main/movies");
        String path1 = "/mypath/main/movies/movie1.mp4";
        String path2 = "/mypath/main/movies/movie2/file1.mp4";
        String path3 = "/mypath/main/movies/mypath/movie3/file3.mkv";
        String path4 = "/mypath/main/movies/movie2.mp4";

        createFile(path1);
        createFile(path2);
        createFile(path3);
        createFile(path4);

        List<Path> firstFiles = service.walk(rootPath, 1);
        assertEquals(2, firstFiles.size());
        assertEquals(path1, firstFiles.get(0).toAbsolutePath().toString());
        assertEquals(path4, firstFiles.get(1).toAbsolutePath().toString());

        List<Path> secondFiles = service.walk(rootPath, 2);
        assertEquals(3, secondFiles.size());
        assertEquals(path1, secondFiles.get(0).toAbsolutePath().toString());
        assertEquals(path2, secondFiles.get(1).toAbsolutePath().toString());
        assertEquals(path4, secondFiles.get(2).toAbsolutePath().toString());

        List<Path> thirdFiles = service.walk(rootPath, 3);
        assertEquals(4, thirdFiles.size());
        assertEquals(path1, thirdFiles.get(0).toAbsolutePath().toString());
        assertEquals(path2, thirdFiles.get(1).toAbsolutePath().toString());
        assertEquals(path4, thirdFiles.get(2).toAbsolutePath().toString());
        assertEquals(path3, thirdFiles.get(3).toAbsolutePath().toString());
    }

    @Test
    @DisplayName("Service can walk a path at least depth 1")
    public void walkDepthCannotBeZero() throws IOException {
        LocalPath rootPath = service.produceLocalPath("/mypath");
        createFile("/mypath/doesntMatter.txt");
        assertThrows(IllegalArgumentException.class, () -> {
            service.walk(rootPath, 0);
        });
    }

    @Test
    @DisplayName("Service can walk a directory only")
    public void walkPathMustBeDirectory() throws IOException {
        LocalPath rootPath = service.produceLocalPath("/mypath");
        assertThrows(NotDirectoryException.class, () -> {
            service.walk(rootPath, 1);
        });
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
