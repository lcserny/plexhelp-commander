package net.cserny.search;

import io.restassured.http.ContentType;
import net.cserny.IntegrationTest;
import net.cserny.filesystem.FilesystemProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class MediaSearchControllerTest extends IntegrationTest {

    @Autowired
    FilesystemProperties filesystemConfig;

    @Autowired
    SearchProperties searchConfig;

    @BeforeEach
    public void setUp() throws IOException {
        deleteDirectory(filesystemConfig.getDownloadsPath());
        createDirectories(filesystemConfig.getDownloadsPath());
    }

    @Test
    @DisplayName("Check search finds correct media")
    public void checkSearchFindsCorrectMedia() throws IOException {
        String downloadPath = filesystemConfig.getDownloadsPath();
        String video1 = downloadPath + "/video1.mp4";
        createFile(6, video1);
        String video2 = downloadPath + "/" + searchConfig.getExcludePaths().get(0) + "/video2.mp4";
        createFile(6, video2);
        String video3 = downloadPath + "/video3.mp4";
        createFile(6, video3);
        String video4 = downloadPath + "/video4.mp4";
        createFile(1, video4);
        String video5 = downloadPath + "/some tvShow/video3.mp4";
        createFile(6, video5);
        String video6 = downloadPath + "/some tvShow/video1.mp4";
        createFile(6, video6);
        String video7 = downloadPath + "/some tvShow/video5.mp4";
        createFile(6, video7);

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
