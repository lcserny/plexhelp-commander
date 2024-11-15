package net.cserny.move;

import net.cserny.filesystem.AbstractInMemoryFileService;
import net.cserny.MongoTestConfiguration;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaMoveError;
import net.cserny.search.MediaIdentificationService;
import net.cserny.search.SearchProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
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
        SearchProperties.class,
        DefaultVideosGrouper.class
})
@DataMongoTest
@Testcontainers
public class MediaMoveServiceTest extends AbstractInMemoryFileService {

    @Autowired
    MediaMoveService service;

    @Autowired
    FilesystemProperties filesystemConfig;
    @Autowired
    private LocalFileService localFileService;

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
        createFile(6, path + "/" + movie);

        MediaFileGroup fileGroup = new MediaFileGroup().path(path).name(name).videos(List.of(movie));
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
        createFile(6, path + "/" + show);

        MediaFileGroup fileGroup = new MediaFileGroup().path(path).name(name).videos(List.of(show)).season(1);
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MediaFileType.TV);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(path, show).path()));
        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.getTvPath(), name, "Season 1", name + " S01.mp4").path()));
    }

    @Test
    @DisplayName("Existing tv with same filename will not get moved")
    public void existingTvShowNameNotMoved() throws IOException {
        String name = "some show";
        String path = filesystemConfig.getDownloadsPath() + "/doesnt matter";
        String show = "myShow.mp4";

        createFile(6, filesystemConfig.getTvPath(), name, "Season 1", name + " S01.mp4");
        createFile(6, path, show);

        MediaFileGroup fileGroup = new MediaFileGroup().path(path).name(name).videos(List.of(show)).season(1);
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MediaFileType.TV);

        assertEquals(1, errors.size());
        assertTrue(Files.exists(fileService.toLocalPath(path, show).path()));
        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.getTvPath(), name, "Season 1", name + " S01.mp4").path()));
    }

    @Test
    @DisplayName("Movie in Downloads root is moved, but Downloads is not cleaned")
    public void movieInDownloadsRootMovedNoClean() throws IOException {
        String randomFile = "someFile.txt";
        createFile(filesystemConfig.getDownloadsPath() + "/" + randomFile);

        String name = "some movieeee";
        String path = filesystemConfig.getDownloadsPath();
        String movie = "mooovee.mp4";

        createFile(6, path + "/" + movie);

        MediaFileGroup fileGroup = new MediaFileGroup().path(path).name(name).videos(List.of(movie));
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MediaFileType.MOVIE);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(path, movie).path()));
        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.getMoviesPath(), name, name + ".mp4").path()));
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
        createFile(6, path + "/" + tv);

        MediaFileGroup fileGroup = new MediaFileGroup().path(path).name(name).videos(List.of(tv)).season(2);
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MediaFileType.TV);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(path, tv).path()));
        String destVideoFile = name + " S02E01.mkv";
        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.getTvPath(), name, "Season 2", destVideoFile).path()));
    }

    @Test
    @DisplayName("Movie with sample video bigger than threshold, moving moves the bigger file")
    public void movieWithSampleBiggerThanThresholdIsMovedCorrectly() throws IOException {
        String name = "MyMovieX";
        String sampleFile = "sample.mp4";
        String movieFile = "The Movie.mp4";

        createFile(6, filesystemConfig.getDownloadsPath(), name, sampleFile);
        createFile(10, filesystemConfig.getDownloadsPath(), name, movieFile);

        MediaFileGroup fileGroup = new MediaFileGroup()
                .path(format("%s/%s", filesystemConfig.getDownloadsPath(), name))
                .name(name)
                .videos(List.of(sampleFile, movieFile));
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MediaFileType.MOVIE);

        assertEquals(0, errors.size());
        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.getMoviesPath(), name, name + ".mp4").path()));
        assertFalse(Files.exists(fileService.toLocalPath(filesystemConfig.getMoviesPath(), name, sampleFile).path()));
    }
}