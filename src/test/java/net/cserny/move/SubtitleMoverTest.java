package net.cserny.move;

import net.cserny.IntegrationTest;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalPath;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaMoveError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubtitleMoverTest extends IntegrationTest {

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

        SubsMoveOperation operation = new SubsMoveOperation(subsSrc, null, null, new MediaFileGroup());
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

        MediaFileGroup group = new MediaFileGroup().name(movieName);

        SubsMoveOperation operation = new SubsMoveOperation(
                fileService.toLocalPath(movieSrc),
                filesystemConfig.getMoviesPath(),
                MediaFileType.MOVIE,
                group
        );

        List<MediaMoveError> errors = mover.moveSubs(operation);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(movieSrc, subName).path()));
        assertTrue(Files.exists(fileService.toLocalPath(movieDest, "some movie.srt").path()));
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

        MediaFileGroup group = new MediaFileGroup().name(showName);

        SubsMoveOperation operation = new SubsMoveOperation(
                fileService.toLocalPath(showSrc),
                filesystemConfig.getTvPath(),
                MediaFileType.TV,
                group
        );

        List<MediaMoveError> errors = mover.moveSubs(operation);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(showSrc, subName).path()));
        assertTrue(Files.exists(fileService.toLocalPath(showDest, "some show.srt").path()));
    }

    @Test
    @DisplayName("TV Show nested subs with duplicate names are moved to Subs subfolder of destination")
    public void nestedTvSubsMove() throws IOException {
        String showName = "some show";
        String nestedSubFolderName = "show.s02e12.1080p";
        String subName = "sub.s02e12.srt";

        String showSrc = filesystemConfig.getDownloadsPath() + "/" + showName;
        createFile(showSrc + "/" + nestedSubFolderName + "/" + subName);

        String showDest = filesystemConfig.getTvPath() + "/" + showName;
        createDirectories(showDest);

        MediaFileGroup group = new MediaFileGroup().name(showName).season(2);

        SubsMoveOperation operation = new SubsMoveOperation(
                fileService.toLocalPath(showSrc),
                filesystemConfig.getTvPath(),
                MediaFileType.TV,
                group
        );

        List<MediaMoveError> errors = mover.moveSubs(operation);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.toLocalPath(showSrc, subName).path()));
        assertTrue(Files.exists(fileService.toLocalPath(showDest, "Season 2", "some show S02E12.srt").path()));
    }
}