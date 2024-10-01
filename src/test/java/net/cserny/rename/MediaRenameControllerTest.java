package net.cserny.rename;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import net.cserny.filesystem.AbstractInMemoryFileService;
import net.cserny.MongoTestConfiguration;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
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
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableAutoConfiguration
@ContextConfiguration(classes = {
        MediaRenameController.class,
        MongoTestConfiguration.class,
        MediaRenameService.class,
        DiskSearcher.class,
        OnlineCacheSearcher.class,
        ExternalSearcher.class,
        NameNormalizer.class,
        FilesystemProperties.class,
        RenameConfig.class,
        OnlineProperties.class,
        TmdbProperties.class,
        RestTemplate.class,
        TMDBSetupMock.class,
        LocalFileService.class
})
@AutoConfigureDataMongo
@Testcontainers
class MediaRenameControllerTest extends AbstractInMemoryFileService {

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
                .body("mediaDescriptions[0].title", is("My Movie"))
                .body("mediaDescriptions[0].date", is("2022"));
    }
}