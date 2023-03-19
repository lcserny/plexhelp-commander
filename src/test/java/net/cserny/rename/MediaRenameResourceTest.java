package net.cserny.rename;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import net.cserny.AbstractInMemoryFileService;
import net.cserny.MongoTestSetup;
import net.cserny.filesystem.FilesystemConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Testcontainers
@QuarkusTestResource(MongoTestSetup.class)
class MediaRenameResourceTest extends AbstractInMemoryFileService {

    @Inject
    FilesystemConfig filesystemConfig;

    @BeforeEach
    public void init() throws IOException {
        createDirectories(filesystemConfig.downloadsPath());
        createDirectories(filesystemConfig.tvShowsPath());
        createDirectories(filesystemConfig.moviesPath());
    }

    @Test
    @DisplayName("Check that the endpoint can produce media rename options")
    public void testCanProduceMediaRenameOptions() {
        MediaRenameRequest request = new MediaRenameRequest("My Movie (2022)", MediaFileType.MOVIE);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/media-renames")
                .then()
                .statusCode(200)
                .body("origin", is(MediaRenameOrigin.NAME.toString()))
                .body("mediaDescriptions[0].title", is("My Movie (2022)"));
    }
}