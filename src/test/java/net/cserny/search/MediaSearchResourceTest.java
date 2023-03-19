package net.cserny.search;

import io.quarkus.test.junit.QuarkusTest;
import net.cserny.AbstractInMemoryFileService;
import net.cserny.filesystem.FilesystemConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class MediaSearchResourceTest extends AbstractInMemoryFileService {

    @Inject
    FilesystemConfig filesystemConfig;

    @Inject
    SearchConfig searchConfig;

    @Test
    @DisplayName("Check search finds correct media")
    public void checkSearchFindsCorrectMedia() throws IOException {
        String downloadPath = filesystemConfig.downloadsPath();
        String video1 = downloadPath + "/video1.mp4";
        createFile(video1, 6);
        String video2 = downloadPath + "/" + searchConfig.excludePaths().get(0) + "/video2.mp4";
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
