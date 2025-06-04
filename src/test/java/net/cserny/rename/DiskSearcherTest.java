package net.cserny.rename;

import net.cserny.filesystem.AbstractInMemoryFileService;
import net.cserny.MongoTestConfiguration;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.generated.MediaDescriptionData;
import net.cserny.generated.MediaFileType;
import net.cserny.rename.NameNormalizer.NameYear;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest({
        "server.command.name=test-server",
        "server.command.listen-cron=disabled",
        "search.video-min-size-bytes=5",
        "search.exclude-paths[0]=Excluded Folder 1"
})
@ContextConfiguration(classes = {
        DiskSearcher.class,
        MongoTestConfiguration.class,
        FilesystemProperties.class,
        RenameConfig.class,
        LocalFileService.class
})
@EnableAutoConfiguration
// FIXME
public class DiskSearcherTest extends AbstractInMemoryFileService {

    @Autowired
    DiskSearcher searcher;

    @Autowired
    FilesystemProperties filesystemConfig;

    @Test
    @DisplayName("Check similar media folders are detected correctly")
    public void checkSimilarMedia() throws IOException {
        createDirectories(filesystemConfig.getMoviesPath() + "/My Coding Movee (2022)");
        createDirectories(filesystemConfig.getMoviesPath() + "/My Codig Movee (2022-12-01)");
        createDirectories(filesystemConfig.getMoviesPath() + "/Another Something (2022)");

        NameYear movie = new NameYear("My Coding Novie", 1918);

        List<MediaDescriptionData> results = searcher.search(movie, MediaFileType.MOVIE).getMediaDescriptions();

        assertEquals(2, results.size());
        assertEquals("My Coding Movee", results.get(0).getTitle());
        assertEquals("2022", results.get(0).getDate());
        assertEquals("My Codig Movee", results.get(1).getTitle());
        assertEquals("2022-12-01", results.get(1).getDate());
    }
}
