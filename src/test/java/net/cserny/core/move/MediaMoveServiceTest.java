package net.cserny.core.move;

import net.cserny.IntegrationTest;
import net.cserny.fs.FilesystemProperties;
import net.cserny.generated.MediaDescriptionData;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaMoveError;
import net.cserny.generated.MovedMediaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

import static java.lang.String.format;
import static net.cserny.generated.MediaFileType.MOVIE;
import static net.cserny.generated.MediaFileType.TV;
import static org.assertj.core.api.Assertions.assertThat;

public class MediaMoveServiceTest extends IntegrationTest {

    private final static MediaDescriptionData emptyDesc = new MediaDescriptionData();

    @Autowired
    private MediaMoveService service;

    @Autowired
    private MovedMediaRepository repository;

    @Autowired
    private FilesystemProperties filesystemConfig;

    @BeforeEach
    public void init() throws IOException {
        createDirectories(filesystemConfig.getDownloadsPath());
        createDirectories(filesystemConfig.getMoviesPath());
        createDirectories(filesystemConfig.getTvPath());

        repository.deleteAll();
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
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MOVIE, emptyDesc);

        assertThat(errors).hasSize(1);
        assertThat(Files.exists(fileService.toLocalPath(path, movie).path())).isTrue();
    }

    @Test
    @DisplayName("Existing tv will get merged moved tv show")
    public void existingTvShowMerge() throws IOException {
        String name = "some shiznit";
        String path = filesystemConfig.getDownloadsPath() + "/shiznit name";
        String show = "myShiznit.mp4";

        createDirectories(filesystemConfig.getTvPath() + "/" + name);
        createFile(6, path + "/" + show);

        MediaFileGroup fileGroup = new MediaFileGroup().path(path).name(name).videos(List.of(show)).season(1);
        List<MediaMoveError> errors = service.moveMedia(fileGroup, TV, emptyDesc);

        assertThat(errors).isEmpty();
        assertThat(Files.exists(fileService.toLocalPath(path, show).path())).isFalse();
        assertThat(Files.exists(fileService.toLocalPath(filesystemConfig.getTvPath(), name, "Season 1", name + " S01.mp4").path())).isTrue();
    }

    @Test
    @DisplayName("Existing tv with same filename will not get moved")
    public void existingTvShowNameNotMoved() throws IOException {
        String name = "some show";
        String path = filesystemConfig.getDownloadsPath() + "/doesnt matter";
        String show = "myShow.mp4";

        createFile(6, filesystemConfig.getTvPath(), name, "Season 1", name + " S01.mp4");
        createFile(6, path, show);

        assertThat(Files.exists(fileService.toLocalPath(path, show).path())).isTrue();
        assertThat(Files.exists(fileService.toLocalPath(filesystemConfig.getTvPath(), name, "Season 1", name + " S01.mp4").path())).isTrue();
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
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MOVIE, emptyDesc);

        assertThat(errors).isEmpty();
        assertThat(Files.exists(fileService.toLocalPath(path, movie).path())).isFalse();
        assertThat(Files.exists(fileService.toLocalPath(filesystemConfig.getMoviesPath(), name, name + ".mp4").path())).isTrue();
        assertThat(Files.exists(fileService.toLocalPath(filesystemConfig.getDownloadsPath(), randomFile).path())).isTrue();
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
        List<MediaMoveError> errors = service.moveMedia(fileGroup, TV, emptyDesc);

        assertThat(errors).isEmpty();
        assertThat(Files.exists(fileService.toLocalPath(path, tv).path())).isFalse();
        String destVideoFile = name + " S02E01.mkv";
        assertThat(Files.exists(fileService.toLocalPath(filesystemConfig.getTvPath(), name, "Season 2", destVideoFile).path())).isTrue();
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
        List<MediaMoveError> errors = service.moveMedia(fileGroup, MOVIE, emptyDesc);

        assertThat(errors).isEmpty();
        assertThat(Files.exists(fileService.toLocalPath(filesystemConfig.getMoviesPath(), name, name + ".mp4").path())).isTrue();
        assertThat(Files.exists(fileService.toLocalPath(filesystemConfig.getMoviesPath(), name, sampleFile).path())).isFalse();
    }

    @Test
    @DisplayName("Moving a media file with mediaDesc populated, persists the media and desc correctly")
    public void movingMediaWithDescriptionPersistsItCorrectly() throws IOException {
        String name = "Asssssssssddddddfg";
        String movieFile = "video.mp4";

        createFile(10, filesystemConfig.getDownloadsPath(), name, movieFile);

        MediaFileGroup fileGroup = new MediaFileGroup()
                .path(format("%s/%s", filesystemConfig.getDownloadsPath(), name))
                .name(name)
                .videos(List.of(movieFile));

        MediaDescriptionData mediaDescriptionData = new MediaDescriptionData();
        mediaDescriptionData.setDescription("This is a description");
        mediaDescriptionData.setTitle("This is a title");
        mediaDescriptionData.setDate(LocalDate.now().toString());
        mediaDescriptionData.setPosterUrl("https://www.poster.com");
        mediaDescriptionData.setCast(List.of("A", "B"));

        List<MediaMoveError> errors = service.moveMedia(fileGroup, MOVIE, mediaDescriptionData);

        assertThat(errors).isEmpty();
        assertThat(Files.exists(fileService.toLocalPath(filesystemConfig.getMoviesPath(), name, name + ".mp4").path())).isTrue();

        List<MovedMedia> movedMedia = repository.findAllByMediaName(name);
        assertThat(movedMedia).hasSize(1);
        assertThat(movedMedia.getFirst())
                .extracting("mediaName", "mediaDesc")
                .containsExactly(name, mediaDescriptionData);
    }

    @Test
    @DisplayName("When retrieving all available moved media, only the undeleted media files are returned")
    public void getAvailableMovedMediaReturnedDoesNotContainDeletedMedia() {
        MovedMedia undeletedMedia1 = MovedMedia.builder().mediaName("A").deleted(false).build();
        MovedMedia undeletedMedia2 = MovedMedia.builder().mediaName("B").deleted(false).build();
        MovedMedia deletedMedia = MovedMedia.builder().mediaName("C").deleted(true).build();
        repository.saveAll(List.of(undeletedMedia1, undeletedMedia2, deletedMedia));

        List<MovedMediaData> availableMovedMedia = service.getAvailableMovedMedia();

        assertThat(availableMovedMedia)
                .hasSize(2)
                .noneMatch(MovedMediaData::getDeleted);
    }
}