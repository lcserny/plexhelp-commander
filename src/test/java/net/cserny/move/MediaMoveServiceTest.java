package net.cserny.move;

import net.cserny.AbstractInMemoryFileService;
import net.cserny.MongoTestConfiguration;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.rename.MediaFileType;
import net.cserny.search.MediaFileGroup;
import net.cserny.search.MediaIdentificationService;
import net.cserny.search.SearchProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ContextConfiguration(classes = {
        MediaMoveService.class,
        SubtitleMover.class,
        FilesystemProperties.class,
        MoveProperties.class,
        LocalFileService.class,
        MongoTestConfiguration.class,
        MediaIdentificationService.class,
        SearchProperties.class
})
@DataMongoTest
@Testcontainers
public class MediaMoveServiceTest extends AbstractInMemoryFileService {

    @Autowired
    MediaMoveService service;

    @Autowired
    FilesystemProperties filesystemConfig;

    @BeforeEach
    public void init() throws IOException {
        createDirectories(filesystemConfig.getDownloadsPath());
        createDirectories(filesystemConfig.getMoviesPath());
        createDirectories(filesystemConfig.getTvPath());
    }

    @Test
    @DisplayName("Existing movie will give move error")
    public void existingMovieError() throws IOException {
        String name = "some movie";
        String path = filesystemConfig.getDownloadsPath() + "/doesnt matter";
        String movie = "myMovie.mp4";

        createDirectories(filesystemConfig.getMoviesPath() + "/" + name);
        createFile(path + "/" + movie, 6);

        MediaFileGroup fileGroup = new MediaFileGroup(path, name, List.of(movie));
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MediaFileType.MOVIE);

        assertEquals(1, errors.size());
        assertTrue(Files.exists(fileService.toLocalPath(path, movie).path()));
    }

    @Test
    @DisplayName("Existing tv will get merged moved tv show")
    public void existingTvShowMerge() throws IOException {
        String name = "some show";
        String path = filesystemConfig.getDownloadsPath() + "/doesnt matter";
        String show = "myShow.mp4";

        createDirectories(filesystemConfig.getTvPath() + "/" + name);
        createFile(path + "/" + show, 6);

        MediaFileGroup fileGroup = new MediaFileGroup(path, name, List.of(show));
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MediaFileType.TV);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(path, show).path()));
        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.getTvPath(), name, show).path()));
    }

    @Test
    @DisplayName("Movie in Downloads root is moved, but Downloads is not cleaned")
    public void movieInDownloadsRootMovedNoClean() throws IOException {
        String randomFile = "someFile.txt";
        createFile(filesystemConfig.getDownloadsPath() + "/" + randomFile);

        String name = "some movieeee";
        String path = filesystemConfig.getDownloadsPath();
        String movie = "mooovee.mp4";

        createFile(path + "/" + movie, 6);

        MediaFileGroup fileGroup = new MediaFileGroup(path, name, List.of(movie));
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MediaFileType.MOVIE);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(path, movie).path()));
        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.getMoviesPath(), name, movie).path()));
        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.getDownloadsPath(), randomFile).path()));
    }

    @Test
    @DisplayName("TV show with subdir, is moved without subdir")
    public void moveTvShowWithoutSubdir() throws IOException {
        String name = "House of the Dragon S02E01 1080p REPACK AMZN WEB-DL DDP5 1 H 264-NTb[TGx]";
        String subdir = "House.of.the.Dragon.S02E01.1080p.REPACK.AMZN.WEB-DL.DDP5.1.H.264-NTb";
        String path = format("%s/%s", filesystemConfig.getDownloadsPath(), name);
        String videoFile = "House.of.the.Dragon.S02E01.1080p.REPACK.AMZN.WEB-DL.DDP5.1.H.264-NTb.mkv";
        String tv = format("%s/%s", subdir, videoFile);
        createFile(path + "/" + tv, 6);

        MediaFileGroup fileGroup = new MediaFileGroup(path, name, List.of(tv));
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MediaFileType.TV);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(path, tv).path()));
        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.getTvPath(), name, videoFile).path()));
    }
}