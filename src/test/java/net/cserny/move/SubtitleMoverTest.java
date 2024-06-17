package net.cserny.move;

import net.cserny.AbstractInMemoryFileService;
import net.cserny.MongoTestConfiguration;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.rename.MediaFileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static net.cserny.move.SubtitleMover.SUBS_SUBFOLDER;
import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {
        SubtitleMover.class,
        FilesystemProperties.class,
        MoveProperties.class,
        LocalFileService.class,
        MongoTestConfiguration.class,
})
@DataMongoTest
@Testcontainers
class SubtitleMoverTest extends AbstractInMemoryFileService {

    @Autowired
    SubtitleMover mover;

    @Autowired
    FilesystemProperties filesystemConfig;

    @BeforeEach
    public void init() throws IOException {
        createDirectories(filesystemConfig.getDownloadsPath());
        createDirectories(filesystemConfig.getMoviesPath());
        createDirectories(filesystemConfig.getTvPath());
    }

    @Test
    @DisplayName("Sub search skipped if media is in root of Downloads")
    public void rootDownloadsSubSkip() throws IOException {
        String subFile = filesystemConfig.getDownloadsPath() + "/mysub.srt";
        createFile(subFile);

        LocalPath subsSrc = fileService.toLocalPath(filesystemConfig.getDownloadsPath());

        SubsMoveOperation operation = new SubsMoveOperation(subsSrc, null, null);
        List<MediaMoveError> errors = mover.moveSubs(operation);

        assertEquals(0, errors.size());
        assertTrue(Files.exists(fileService.toLocalPath(subFile).path()));
    }

    @Test
    @DisplayName("Movie subs are moved in movie destination")
    public void movieSubsMovedToDest() throws IOException {
        String movieName = "some movie";
        String subName = "sub.srt";

        String movieSrc = filesystemConfig.getDownloadsPath() + "/" + movieName;
        createFile(movieSrc + "/" + subName);

        String movieDest = filesystemConfig.getMoviesPath() + "/" + movieName;
        createDirectories(movieDest);

        SubsMoveOperation operation = new SubsMoveOperation(
                fileService.toLocalPath(movieSrc),
                fileService.toLocalPath(movieDest),
                MediaFileType.MOVIE
        );

        List<MediaMoveError> errors = mover.moveSubs(operation);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(movieSrc, subName).path()));
        assertTrue(Files.exists(fileService.toLocalPath(movieDest, subName).path()));
    }

    @Test
    @DisplayName("TV Show subs are moved to Subs subfolder of destination")
    public void tvSubsMovedToSubfolder() throws IOException {
        String showName = "some show";
        String subName = "sub.srt";

        String showSrc = filesystemConfig.getDownloadsPath() + "/" + showName;
        createFile(showSrc + "/" + subName);

        String showDest = filesystemConfig.getTvPath() + "/" + showName;
        createDirectories(showDest);

        SubsMoveOperation operation = new SubsMoveOperation(
                fileService.toLocalPath(showSrc),
                fileService.toLocalPath(showDest),
                MediaFileType.TV
        );

        List<MediaMoveError> errors = mover.moveSubs(operation);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(showSrc, subName).path()));
        assertTrue(Files.exists(fileService.toLocalPath(showDest, SUBS_SUBFOLDER, subName).path()));
    }

    @Test
    @DisplayName("TV Show nested subs with duplicate names are moved to Subs subfolder of destination")
    public void nestedTvSubsMove() throws IOException {
        String showName = "some show";
        String nestedSubFolderName = "show.s02e12.1080p";
        String subName = "sub.srt";

        String showSrc = filesystemConfig.getDownloadsPath() + "/" + showName;
        createFile(showSrc + "/" + nestedSubFolderName + "/" + subName);

        String showDest = filesystemConfig.getTvPath() + "/" + showName;
        createDirectories(showDest);

        SubsMoveOperation operation = new SubsMoveOperation(
                fileService.toLocalPath(showSrc),
                fileService.toLocalPath(showDest),
                MediaFileType.TV
        );

        List<MediaMoveError> errors = mover.moveSubs(operation);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(showSrc, subName).path()));
        assertTrue(Files.exists(fileService.toLocalPath(showDest, SUBS_SUBFOLDER, nestedSubFolderName + "." + subName).path()));
    }
}