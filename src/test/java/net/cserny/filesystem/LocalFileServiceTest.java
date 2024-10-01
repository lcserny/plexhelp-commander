package net.cserny.filesystem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = {
        LocalFileService.class,
})
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
    @DisplayName("Service can delete a directory")
    public void deleteDirectoryWorks() throws IOException {
        createFile("/hello/my.txt");
        LocalPath directory = fileService.toLocalPath("/hello");

        assertTrue(Files.exists(directory.path()));

        fileService.deleteDirectory(directory);

        assertFalse(Files.exists(directory.path()));
    }

    @Test
    @DisplayName("Service can move a path")
    public void moveWorks() throws IOException {
        LocalPath localPath = createFile("/hmmm/test.txt");
        LocalPath destPath = fileService.toLocalPath("/someOther/path/text.txt");

        assertTrue(Files.exists(localPath.path()));
        assertFalse(Files.exists(destPath.path()));

        fileService.move(localPath, destPath);

        assertFalse(Files.exists(localPath.path()));
        assertTrue(Files.exists(destPath.path()));
    }

    @Test
    @DisplayName("Service can walk a path")
    public void walkWorks() throws IOException {
        String path1 = "/mypath/file1.txt";
        String path2 = "/mypath/file2.txt";
        String path3 = "/mypath/sub1/file3.txt";
        String path4 = "/mypath/file4.txt";

        createFile(path1);
        createFile(path2);
        createFile(path3);
        createFile(path4);

        LocalPath rootPath = fileService.toLocalPath("/mypath");

        List<LocalPath> filesFound = fileService.walk(rootPath, 1);

        assertEquals(3, filesFound.size());
        assertEquals(path1, filesFound.get(0).path().toAbsolutePath().toString());
        assertEquals(path2, filesFound.get(1).path().toAbsolutePath().toString());
        assertEquals(path4, filesFound.get(2).path().toAbsolutePath().toString());
    }

    @Test
    @DisplayName("Service can walk a path of max depth passed from path passed")
    public void walkDepthIsCorrect() throws IOException {
        String path1 = "/aaa/main/movies/movie1.mp4";
        String path2 = "/aaa/main/movies/movie2/file1.mp4";
        String path3 = "/aaa/main/movies/mypath/movie3/file3.mkv";
        String path4 = "/aaa/main/movies/movie2.mp4";

        createFile(path1);
        createFile(path2);
        createFile(path3);
        createFile(path4);

        LocalPath rootPath = fileService.toLocalPath("/aaa/main/movies");

        List<LocalPath> firstFiles = fileService.walk(rootPath, 1);
        assertEquals(2, firstFiles.size());
        assertEquals(path1, firstFiles.get(0).path().toAbsolutePath().toString());
        assertEquals(path4, firstFiles.get(1).path().toAbsolutePath().toString());

        List<LocalPath> secondFiles = fileService.walk(rootPath, 2);
        assertEquals(3, secondFiles.size());
        assertEquals(path1, secondFiles.get(0).path().toAbsolutePath().toString());
        assertEquals(path2, secondFiles.get(1).path().toAbsolutePath().toString());
        assertEquals(path4, secondFiles.get(2).path().toAbsolutePath().toString());

        List<LocalPath> thirdFiles = fileService.walk(rootPath, 3);
        assertEquals(4, thirdFiles.size());
        assertEquals(path1, thirdFiles.get(0).path().toAbsolutePath().toString());
        assertEquals(path2, thirdFiles.get(1).path().toAbsolutePath().toString());
        assertEquals(path4, thirdFiles.get(2).path().toAbsolutePath().toString());
        assertEquals(path3, thirdFiles.get(3).path().toAbsolutePath().toString());
    }

    @Test
    @DisplayName("Service can walk a path at least depth 1")
    public void walkDepthCannotBeZero() throws IOException {
        createFile("/bbb/doesntMatter.txt");
        LocalPath rootPath = fileService.toLocalPath("/bbb");
        assertThrows(IllegalArgumentException.class, () -> {
            fileService.walk(rootPath, 0);
        });
    }

    @Test
    @DisplayName("Service can walk a directory only")
    public void walkPathMustBeDirectory() throws IOException {
        LocalPath rootPath = fileService.toLocalPath("/ccc");
        assertThrows(NotDirectoryException.class, () -> {
            fileService.walk(rootPath, 1);
        });
    }
}
