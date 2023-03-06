package net.cserny.filesystem;

import io.quarkus.test.junit.QuarkusTest;
import net.cserny.AbstractInMemoryFileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class LocalFileServiceTest extends AbstractInMemoryFileService {

    @Test
    @DisplayName("Service can delete a path")
    public void deleteWorks() throws IOException {
        LocalPath localPath = createFile("/hello/my.txt");

        assertTrue(Files.exists(localPath.path()));

        fileService.delete(localPath);

        assertFalse(Files.exists(localPath.path()));
    }

    @Test
    @DisplayName("Service can move a path")
    public void moveWorks() throws IOException {
        LocalPath localPath = createFile("/hmmm/test.txt");
        LocalPath destPath = fileService.produceLocalPath("/someOther/path/text.txt");

        assertTrue(Files.exists(localPath.path()));
        assertFalse(Files.exists(destPath.path()));

        fileService.move(localPath, destPath);

        assertFalse(Files.exists(localPath.path()));
        assertTrue(Files.exists(destPath.path()));
    }

    @Test
    @DisplayName("Service can walk a path")
    public void walkWorks() throws IOException {
        LocalPath rootPath = fileService.produceLocalPath("/mypath");
        String path1 = "/mypath/file1.txt";
        String path2 = "/mypath/file2.txt";
        String path3 = "/mypath/sub1/file3.txt";
        String path4 = "/mypath/file4.txt";

        createFile(path1);
        createFile(path2);
        createFile(path3);
        createFile(path4);

        List<Path> filesFound = fileService.walk(rootPath, 1);

        assertEquals(3, filesFound.size());
        assertEquals(path1, filesFound.get(0).toAbsolutePath().toString());
        assertEquals(path2, filesFound.get(1).toAbsolutePath().toString());
        assertEquals(path4, filesFound.get(2).toAbsolutePath().toString());
    }

    @Test
    @DisplayName("Service can walk a path of max depth passed from path passed")
    public void walkDepthIsCorrect() throws IOException {
        LocalPath rootPath = fileService.produceLocalPath("/aaa/main/movies");
        String path1 = "/aaa/main/movies/movie1.mp4";
        String path2 = "/aaa/main/movies/movie2/file1.mp4";
        String path3 = "/aaa/main/movies/mypath/movie3/file3.mkv";
        String path4 = "/aaa/main/movies/movie2.mp4";

        createFile(path1);
        createFile(path2);
        createFile(path3);
        createFile(path4);

        List<Path> firstFiles = fileService.walk(rootPath, 1);
        assertEquals(2, firstFiles.size());
        assertEquals(path1, firstFiles.get(0).toAbsolutePath().toString());
        assertEquals(path4, firstFiles.get(1).toAbsolutePath().toString());

        List<Path> secondFiles = fileService.walk(rootPath, 2);
        assertEquals(3, secondFiles.size());
        assertEquals(path1, secondFiles.get(0).toAbsolutePath().toString());
        assertEquals(path2, secondFiles.get(1).toAbsolutePath().toString());
        assertEquals(path4, secondFiles.get(2).toAbsolutePath().toString());

        List<Path> thirdFiles = fileService.walk(rootPath, 3);
        assertEquals(4, thirdFiles.size());
        assertEquals(path1, thirdFiles.get(0).toAbsolutePath().toString());
        assertEquals(path2, thirdFiles.get(1).toAbsolutePath().toString());
        assertEquals(path4, thirdFiles.get(2).toAbsolutePath().toString());
        assertEquals(path3, thirdFiles.get(3).toAbsolutePath().toString());
    }

    @Test
    @DisplayName("Service can walk a path at least depth 1")
    public void walkDepthCannotBeZero() throws IOException {
        LocalPath rootPath = fileService.produceLocalPath("/bbb");
        createFile("/bbb/doesntMatter.txt");
        assertThrows(IllegalArgumentException.class, () -> {
            fileService.walk(rootPath, 0);
        });
    }

    @Test
    @DisplayName("Service can walk a directory only")
    public void walkPathMustBeDirectory() throws IOException {
        LocalPath rootPath = fileService.produceLocalPath("/ccc");
        assertThrows(NotDirectoryException.class, () -> {
            fileService.walk(rootPath, 1);
        });
    }
}
