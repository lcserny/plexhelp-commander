package net.cserny.web;

import io.restassured.http.ContentType;
import net.cserny.config.FilesystemProperties;
import net.cserny.core.move.MovedMedia;
import net.cserny.core.move.MovedMediaRepository;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaMoveRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MediaMoveControllerTest extends WebIntegrationTest {

    @Autowired
    FilesystemProperties filesystemConfig;

    @Autowired
    MovedMediaRepository repository;

    @Test
    @DisplayName("Check endpoint moves media correctly")
    public void testMoveCorrectly() throws IOException {
        String path = filesystemConfig.getDownloadsPath() + "/My Movie";
        String name = "MY Movee";
        String video = "file.mp4";

        createFile(6, path + "/" + video);

        MediaFileGroup fileGroup = new MediaFileGroup().path(path).name(name).videos(List.of(video));
        MediaMoveRequest request = new MediaMoveRequest().fileGroup(fileGroup).type(MediaFileType.MOVIE);

        given().contentType(ContentType.JSON).body(request)
                .when()
                .post("/api/v1/media-moves")
                .then()
                .statusCode(200)
                .body("$.size()", is(0));
    }

    @Test
    @DisplayName("DELETE endpoint removes TV show directory when last episode deleted")
    public void deleteLastTvEpisodeRemovesShowDir() throws IOException {
        String showName = "ControllerTestShow";
        String seasonDir = format("%s/%s/Season 2", filesystemConfig.getTvPath(), showName);
        createDirectories(seasonDir);
        String ep = "episode.mp4";
        createFile(seasonDir + "/" + ep);

        MovedMedia rec = repository.save(MovedMedia.builder()
                .destination(seasonDir + "/" + ep)
                .mediaName(showName)
                .mediaType(MediaFileType.TV)
                .deleted(false)
                .build());


        given().when()
                .delete("/api/v1/media-moves/{movedMediaId}", rec.getId().toHexString())
                .then()
                .statusCode(200);

        String showPath = format("%s/%s", filesystemConfig.getTvPath(), showName);
        assertTrue(Files.notExists(fileService.toLocalPath(showPath).path()), "Show directory should be deleted");
    }

    @Test
    @DisplayName("POST delete-all removes multiple moved media records and their files")
    public void removeAllMedia_deletesMultipleRecords() throws IOException {
        String moviesPath = filesystemConfig.getMoviesPath();
        String movie1Dir = moviesPath + "/Movie1";
        String movie2Dir = moviesPath + "/Movie2";
        createDirectories(movie1Dir);
        createDirectories(movie2Dir);
        String file1 = "file1.mp4";
        String file2 = "file2.mp4";
        createFile(movie1Dir + "/" + file1);
        createFile(movie2Dir + "/" + file2);

        MovedMedia rec1 = repository.save(MovedMedia.builder()
                .destination(movie1Dir + "/" + file1)
                .mediaName("Movie1")
                .mediaType(MediaFileType.MOVIE)
                .deleted(false)
                .build());

        MovedMedia rec2 = repository.save(MovedMedia.builder()
                .destination(movie2Dir + "/" + file2)
                .mediaName("Movie2")
                .mediaType(MediaFileType.MOVIE)
                .deleted(false)
                .build());

        given().contentType(ContentType.JSON)
                .body(List.of(rec1.getId().toHexString(), rec2.getId().toHexString()))
                .when()
                .post("/api/v1/media-moves/delete-all")
                .then()
                .statusCode(200);

        assertTrue(repository.findById(rec1.getId()).orElseThrow().getDeleted());
        assertTrue(repository.findById(rec2.getId()).orElseThrow().getDeleted());
        assertTrue(Files.notExists(fileService.toLocalPath(movie1Dir + "/" + file1).path()));
        assertTrue(Files.notExists(fileService.toLocalPath(movie2Dir + "/" + file2).path()));
    }
}