package net.cserny.rename;

import io.restassured.http.ContentType;
import net.cserny.IntegrationTest;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaRenameOrigin;
import net.cserny.generated.MediaRenameRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@Import(TMDBSetupMock.class)
class MediaRenameControllerTest extends IntegrationTest {

    @Autowired
    FilesystemProperties filesystemConfig;

    @BeforeEach
    public void init() throws IOException {
        createDirectories(filesystemConfig.getDownloadsPath());
        createDirectories(filesystemConfig.getTvPath());
        createDirectories(filesystemConfig.getMoviesPath());
    }

    @Test
    @DisplayName("Check that the endpoint can produce media rename options")
    public void testCanProduceMediaRenameOptions() {
        MediaRenameRequest request = new MediaRenameRequest().name("My Movie (2022)").type(MediaFileType.MOVIE);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/media-renames")
                .then()
                .statusCode(200)
                .body("origin", is(MediaRenameOrigin.NAME.getValue()))
                .body("mediaDescriptions[0].title", is("My Movie"))
                .body("mediaDescriptions[0].date", is("2022"));
    }
}