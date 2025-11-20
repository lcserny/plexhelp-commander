package net.cserny.move;

import io.restassured.http.ContentType;
import net.cserny.IntegrationTest;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaMoveRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class MediaMoveControllerTest extends IntegrationTest {

    @Autowired
    FilesystemProperties filesystemConfig;

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