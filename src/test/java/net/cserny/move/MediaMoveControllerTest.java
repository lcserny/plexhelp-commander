package net.cserny.move;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import net.cserny.AbstractInMemoryFileService;
import net.cserny.download.DownloadedMediaRepository;
import net.cserny.download.MediaDownloadController;
import net.cserny.download.MediaDownloadService;
import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;
import net.cserny.rename.MediaFileType;
import net.cserny.search.MediaFileGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;


import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(value = {
        "server.command.name=test-server",
        "server.command.listen-cron=disabled",
        "search.video-min-size-bytes=5",
        "search.exclude-paths[0]=Excluded Folder 1"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {
        MediaMoveController.class,
        MediaMoveService.class,
        SubtitleMover.class,
        FilesystemConfig.class,
        MoveConfig.class,
        LocalFileService.class
})
@EnableAutoConfiguration(exclude = MongoAutoConfiguration.class)
public class MediaMoveControllerTest extends AbstractInMemoryFileService {

    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @Autowired
    FilesystemConfig filesystemConfig;

    @BeforeEach
    public void init() throws IOException {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;

        createDirectories(filesystemConfig.getDownloadsPath());
        createDirectories(filesystemConfig.getTvPath());
        createDirectories(filesystemConfig.getMoviesPath());
    }

    @Test
    @DisplayName("Check endpoint moves media correctly")
    public void testMoveCorrectly() throws IOException {
        String path = filesystemConfig.getDownloadsPath() + "/My Movie";
        String name = "MY Movee";
        String video = "file.mp4";

        createFile(path + "/" + video, 6);

        MediaFileGroup fileGroup = new MediaFileGroup(path, name, List.of(video));
        MediaMoveRequest request = new MediaMoveRequest(fileGroup, MediaFileType.MOVIE);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/media-moves")
                .then()
                .statusCode(200)
                .body("$.size()", is(0));
    }
}