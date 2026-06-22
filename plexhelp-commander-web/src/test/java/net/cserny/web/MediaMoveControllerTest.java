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
}