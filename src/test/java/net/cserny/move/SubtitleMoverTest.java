package net.cserny.move;

import io.quarkus.test.junit.QuarkusTest;
import net.cserny.AbstractInMemoryFileService;
import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalPath;
import net.cserny.rename.MediaFileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static net.cserny.move.SubtitleMover.SUBS_SUBFOLDER;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SubtitleMoverTest extends AbstractInMemoryFileService {

    @Inject
    SubtitleMover mover;

    @Inject
    FilesystemConfig filesystemConfig;

    @BeforeEach
    public void init() throws IOException {
        createDirectories(filesystemConfig.downloadsPath());
        createDirectories(filesystemConfig.moviesPath());
        createDirectories(filesystemConfig.tvShowsPath());
    }

    @Test
    @DisplayName("Sub search skipped if media is in root of Downloads")
    public void rootDownloadsSubSkip() throws IOException {
        String subFile = filesystemConfig.downloadsPath() + "/mysub.srt";
        createFile(subFile);

        LocalPath subsSrc = fileService.produceLocalPath(filesystemConfig.downloadsPath());

        SubsMoveOperation operation = new SubsMoveOperation(subsSrc, null, null);
        List<MediaMoveError> errors = mover.moveSubs(operation);

        assertEquals(0, errors.size());
        assertTrue(Files.exists(fileService.produceLocalPath(subFile).path()));
    }

    @Test
    @DisplayName("Movie subs are moved in movie destination")
    public void movieSubsMovedToDest() throws IOException {
        String movieName = "some movie";
        String subName = "sub.srt";

        String movieSrc = filesystemConfig.downloadsPath() + "/" + movieName;
        createFile(movieSrc + "/" + subName);

        String movieDest = filesystemConfig.moviesPath() + "/" + movieName;
        createDirectories(movieDest);

        SubsMoveOperation operation = new SubsMoveOperation(
                fileService.produceLocalPath(movieSrc),
                fileService.produceLocalPath(movieDest),
                MediaFileType.MOVIE
        );

        List<MediaMoveError> errors = mover.moveSubs(operation);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.produceLocalPath(movieSrc, subName).path()));
        assertTrue(Files.exists(fileService.produceLocalPath(movieDest, subName).path()));
    }

    @Test
    @DisplayName("TV Show subs are moved to Subs subfolder of destination")
    public void tvSubsMovedToSubfolder() throws IOException {
        String showName = "some show";
        String subName = "sub.srt";

        String showSrc = filesystemConfig.downloadsPath() + "/" + showName;
        createFile(showSrc + "/" + subName);

        String showDest = filesystemConfig.tvShowsPath() + "/" + showName;
        createDirectories(showDest);

        SubsMoveOperation operation = new SubsMoveOperation(
                fileService.produceLocalPath(showSrc),
                fileService.produceLocalPath(showDest),
                MediaFileType.TV
        );

        List<MediaMoveError> errors = mover.moveSubs(operation);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.produceLocalPath(showSrc, subName).path()));
        assertTrue(Files.exists(fileService.produceLocalPath(showDest, SUBS_SUBFOLDER, subName).path()));
    }

    @Test
    @DisplayName("TV Show nested subs with duplicate names are moved to Subs subfolder of destination")
    public void nestedTvSubsMove() throws IOException {
        String showName = "some show";
        String nestedSubFolderName = "show.s02e12.1080p";
        String subName = "sub.srt";

        String showSrc = filesystemConfig.downloadsPath() + "/" + showName;
        createFile(showSrc + "/" + nestedSubFolderName + "/" + subName);

        String showDest = filesystemConfig.tvShowsPath() + "/" + showName;
        createDirectories(showDest);

        SubsMoveOperation operation = new SubsMoveOperation(
                fileService.produceLocalPath(showSrc),
                fileService.produceLocalPath(showDest),
                MediaFileType.TV
        );

        List<MediaMoveError> errors = mover.moveSubs(operation);

        assertEquals(0, errors.size());
        assertFalse(Files.exists(fileService.produceLocalPath(showSrc, subName).path()));
        assertTrue(Files.exists(fileService.produceLocalPath(showDest, SUBS_SUBFOLDER, nestedSubFolderName + "." + subName).path()));
    }
}