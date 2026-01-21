package net.cserny.move.subtitle;

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

import static net.cserny.generated.MediaFileType.MOVIE;
import static net.cserny.generated.MediaFileType.TV;
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
                MOVIE,
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

    @Test
    @DisplayName("Movie with multiple subs with / without langs defined")
    public void movieWithMultipleSubs() throws IOException {
        String movieName = "Avengers: Doomsday (2025)";
        String subName1 = "movie english.srt";
        String subName2 = "movie en.srt";
        String subName3 = "movie.srt";
        String subName4 = "movie (alternate).srt";

        String movieSrc = filesystemConfig.getDownloadsPath() + "/" + movieName;
        createFile(movieSrc + "/" + subName1);
        createFile(movieSrc + "/" + subName2);
        createFile(movieSrc + "/" + subName3);
        createFile(movieSrc + "/" + subName4);

        String movieDest = filesystemConfig.getMoviesPath() + "/" + movieName;
        createDirectories(movieDest);

        MediaFileGroup group = new MediaFileGroup().name(movieName);
        SubsMoveOperation operation = new SubsMoveOperation(fileService.toLocalPath(movieSrc), filesystemConfig.getMoviesPath(), MOVIE, group);

        List<MediaMoveError> errors = mover.moveSubs(operation);

        assertEquals(0, errors.size());
        assertTrue(Files.exists(fileService.toLocalPath(movieDest, movieName + ".(1).srt").path()));
        assertTrue(Files.exists(fileService.toLocalPath(movieDest, movieName + ".(2).srt").path()));
        assertTrue(Files.exists(fileService.toLocalPath(movieDest, movieName + ".en.(1).srt").path()));
        assertTrue(Files.exists(fileService.toLocalPath(movieDest, movieName + ".en.(2).srt").path()));
    }

    @Test
    @DisplayName("TV show with multiple subs with / without langs defined")
    public void tvWithMultipleSubs() throws IOException {
        String baseName = "Big Bang Theory";
        String date = "(2026-10-10)";
        String showName = baseName + " " + date;
        String subName1 = "show s01e03 english.srt";
        String subName2 = "show.srt";
        String subName3 = "show s01e03 en.srt";
        String subName4 = "show s02e02 en.srt";
        String subName5 = "show s03e01.srt";

        String showSrc = filesystemConfig.getDownloadsPath() + "/" + showName;
        createFile(showSrc + "/" + subName1);
        createFile(showSrc + "/" + subName2);
        createFile(showSrc + "/" + subName3);
        createFile(showSrc + "/" + subName4);
        createFile(showSrc + "/" + subName5);

        String showDest = filesystemConfig.getTvPath() + "/" + showName;
        createDirectories(showDest);

        MediaFileGroup group = new MediaFileGroup().name(showName);
        SubsMoveOperation operation = new SubsMoveOperation(fileService.toLocalPath(showSrc), filesystemConfig.getTvPath(), TV, group);

        List<MediaMoveError> errors = mover.moveSubs(operation);

        assertEquals(0, errors.size());
        assertTrue(Files.exists(fileService.toLocalPath(showDest, "Season 1", baseName + " S01E03 " + date + ".en.(1).srt").path()));
        assertTrue(Files.exists(fileService.toLocalPath(showDest, "Season 1", baseName + " S01E03 " + date + ".en.(2).srt").path()));
        assertTrue(Files.exists(fileService.toLocalPath(showDest, showName + ".srt").path()));
        assertTrue(Files.exists(fileService.toLocalPath(showDest, "Season 2", baseName + " S02E02 " + date + ".en.srt").path()));
        assertTrue(Files.exists(fileService.toLocalPath(showDest, "Season 3", baseName + " S03E01 " + date + ".srt").path()));
    }
}