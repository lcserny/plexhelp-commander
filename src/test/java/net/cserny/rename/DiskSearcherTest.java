package net.cserny.rename;

import net.cserny.AbstractInMemoryFileService;
import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;
import net.cserny.rename.NameNormalizer.NameYear;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
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
        FilesystemConfig.class,
        RenameConfig.class,
        LocalFileService.class
})
@EnableMongoRepositories
@EnableAutoConfiguration
public class DiskSearcherTest extends AbstractInMemoryFileService {

    @Autowired
    DiskSearcher searcher;

    @Autowired
    FilesystemConfig filesystemConfig;

    @Test
    @DisplayName("Check similar media folders are detected correctly")
    public void checkSimilarMedia() throws IOException {
        createDirectories(filesystemConfig.getMoviesPath() + "/My Coding Movee (2022)");
        createDirectories(filesystemConfig.getMoviesPath() + "/My Codig Movee (2022)");
        createDirectories(filesystemConfig.getMoviesPath() + "/Another Something (2022)");

        NameYear movie = new NameYear("My Coding Novie", 1918);

        List<MediaDescription> results = searcher.search(movie, MediaFileType.MOVIE).mediaDescriptions();

        assertEquals(2, results.size());
        assertEquals("My Coding Movee", results.get(0).title());
        assertEquals("My Codig Movee", results.get(1).title());
    }
}
