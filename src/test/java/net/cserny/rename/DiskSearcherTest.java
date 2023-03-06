package net.cserny.rename;

import io.quarkus.test.junit.QuarkusTest;
import net.cserny.AbstractInMemoryFileService;
import net.cserny.filesystem.FilesystemConfig;
import net.cserny.rename.NameNormalizer.NameYear;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class DiskSearcherTest extends AbstractInMemoryFileService {

    @Inject
    DiskSearcher searcher;

    @Inject
    FilesystemConfig filesystemConfig;

    @Test
    @DisplayName("Check similar media folders are detected correctly")
    public void checkSimilarMedia() throws IOException {
        createDirectories(filesystemConfig.moviesPath() + "/My Coding Movee (2022)");
        createDirectories(filesystemConfig.moviesPath() + "/My Codig Movee (2022)");
        createDirectories(filesystemConfig.moviesPath() + "/Another Something (2022)");

        NameYear movie = new NameYear("My Coding Novie", 1918);

        List<String> results = searcher.search(movie, MediaFileType.MOVIE);

        assertEquals(2, results.size());
        assertEquals("My Coding Movee", results.get(0));
        assertEquals("My Codig Movee", results.get(1));
    }
}
