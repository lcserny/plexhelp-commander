package net.cserny.move;

import io.quarkus.test.junit.QuarkusTest;
import net.cserny.AbstractInMemoryFileService;
import net.cserny.filesystem.FilesystemConfig;
import net.cserny.rename.MediaFileType;
import net.cserny.search.MediaFileGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class MediaMoveServiceTest extends AbstractInMemoryFileService {

    @Inject
    MediaMoveService service;

    @Inject
    FilesystemConfig filesystemConfig;

    @BeforeEach
    public void init() throws IOException {
        createDirectories(filesystemConfig.downloadsPath());
        createDirectories(filesystemConfig.moviesPath());
        createDirectories(filesystemConfig.tvShowsPath());
    }

    @Test
    @DisplayName("Existing movie will give move error")
    public void existingMovieError() throws IOException {
        String name = "some movie";
        String path = filesystemConfig.downloadsPath() + "/doesnt matter";
        String movie = "myMovie.mp4";

        createDirectories(filesystemConfig.moviesPath() + "/" + name);
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
        String path = filesystemConfig.downloadsPath() + "/doesnt matter";
        String show = "myShow.mp4";

        createDirectories(filesystemConfig.tvShowsPath() + "/" + name);
        createFile(path + "/" + show, 6);

        MediaFileGroup fileGroup = new MediaFileGroup(path, name, List.of(show));
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MediaFileType.TV);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(path, show).path()));
        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.tvShowsPath(), name, show).path()));
    }

    @Test
    @DisplayName("Movie in Downloads root is moved, but Downloads is not cleaned")
    public void movieInDownloadsRootMovedNoClean() throws IOException {
        String randomFile = "someFile.txt";
        createFile(filesystemConfig.downloadsPath() + "/" + randomFile);

        String name = "some movieeee";
        String path = filesystemConfig.downloadsPath();
        String movie = "mooovee.mp4";

        createFile(path + "/" + movie, 6);

        MediaFileGroup fileGroup = new MediaFileGroup(path, name, List.of(movie));
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MediaFileType.MOVIE);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(path, movie).path()));
        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.moviesPath(), name, movie).path()));
        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.downloadsPath(), randomFile).path()));
    }
}