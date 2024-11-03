package net.cserny.move;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import net.cserny.filesystem.AbstractInMemoryFileService;
import net.cserny.MongoTestConfiguration;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaMoveRequest;
import net.cserny.search.MediaIdentificationService;
import net.cserny.search.SearchProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(value = {
        "server.command.name=test-server",
        "server.command.listen-cron=disabled",
        "search.video-min-size-bytes=5",
        "search.exclude-paths[0]=Excluded Folder 1",
        "automove.enabled=false",
        "automove.initial-delay-ms=5000",
        "automove.cron-ms=10000"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        MediaMoveController.class,
        MediaMoveService.class,
        SubtitleMover.class,
        FilesystemProperties.class,
        MoveProperties.class,
        LocalFileService.class,
        MongoTestConfiguration.class,
        MediaIdentificationService.class,
        SearchProperties.class,
        DefaultVideosParser.class,
        DestinationPathResolver.class
})
@EnableAutoConfiguration
@AutoConfigureDataMongo
@Testcontainers
public class MediaMoveControllerTest extends AbstractInMemoryFileService {

    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @Autowired
    FilesystemProperties filesystemConfig;

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

        createFile(6, path + "/" + video);

        MediaFileGroup fileGroup = new MediaFileGroup().path(path).name(name).videos(List.of(video));
        MediaMoveRequest request = new MediaMoveRequest().fileGroup(fileGroup).type(MediaFileType.MOVIE);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/media-moves")
                .then()
                .statusCode(200)
                .body("$.size()", is(0));
    }
}