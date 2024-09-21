package net.cserny.search;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import net.cserny.AbstractInMemoryFileService;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(value = {
        "server.command.name=test-server",
        "server.command.listen-cron=disabled",
        "search.video-min-size-bytes=5",
        "search.exclude-paths[0]=Excluded Folder 1"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        MediaSearchController.class,
        MediaSearchService.class,
        FilesystemProperties.class,
        SearchProperties.class,
        LocalFileService.class,
        MediaIdentificationService.class
})
@EnableAutoConfiguration(exclude = MongoAutoConfiguration.class)
public class MediaSearchControllerTest extends AbstractInMemoryFileService {

    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @Autowired
    FilesystemProperties filesystemConfig;

    @Autowired
    SearchProperties searchConfig;

    @Test
    @DisplayName("Check search finds correct media")
    public void checkSearchFindsCorrectMedia() throws IOException {
        String downloadPath = filesystemConfig.getDownloadsPath();
        String video1 = downloadPath + "/video1.mp4";
        createFile(video1, 6);
        String video2 = downloadPath + "/" + searchConfig.getExcludePaths().get(0) + "/video2.mp4";
        createFile(video2, 6);
        String video3 = downloadPath + "/video3.mp4";
        createFile(video3, 6);
        String video4 = downloadPath + "/video4.mp4";
        createFile(video4, 1);
        String video5 = downloadPath + "/some tvShow/video3.mp4";
        createFile(video5, 6);
        String video6 = downloadPath + "/some tvShow/video1.mp4";
        createFile(video6, 6);
        String video7 = downloadPath + "/some tvShow/video5.mp4";
        createFile(video7, 6);

        given()
                .contentType(ContentType.JSON)
                .when().get("/api/v1/media-searches")
                .then()
                .statusCode(200)
                .body("$.size()", is(3))
                .body("[0].path", is(downloadPath))
                .body("[0].name", is("video1"))
                .body("[2].path", is(downloadPath + "/some tvShow"))
                .body("[2].name", is("some tvShow"))
                .body("[2].videos[0]", is("video1.mp4"))
                .body("[2].videos[1]", is("video3.mp4"))
                .body("[2].videos[2]", is("video5.mp4"));
    }
}
