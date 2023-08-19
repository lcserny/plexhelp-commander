package net.cserny.rename;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(value = {
        "server.command.name=test-server",
        "server.command.listen-cron=disabled",
        "search.video-min-size-bytes=5",
        "search.exclude-paths[0]=Excluded Folder 1"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMongoRepositories
@EnableAutoConfiguration
@ContextConfiguration(classes = {
        MediaRenameController.class,
        MediaRenameService.class,
        DiskSearcher.class,
        OnlineCacheSearcher.class,
        TMDBSearcher.class,
        NameNormalizer.class,
        FilesystemProperties.class,
        RenameConfig.class,
        OnlineProperties.class,
        TmdbProperties.class,
        RestTemplate.class,
        TMDBSetupMock.class,
        LocalFileService.class
})
@Testcontainers
class MediaRenameControllerTest extends AbstractInMemoryFileService {

    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @Container
    public static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:5.0");

    @DynamicPropertySource
    public static void qTorrentProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoContainer.getConnectionString());
    }

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
                .body("mediaDescriptions[0].title", is("My Movie (2022)"));
    }
}