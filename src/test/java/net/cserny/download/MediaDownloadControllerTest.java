package net.cserny.download;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(value = {
        "server.command.name=test-server",
        "server.command.listen-cron=disabled",
        "search.video-min-size-bytes=5",
        "search.exclude-paths[0]=Excluded Folder 1"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {
        DownloadedMediaRepository.class,
        MediaDownloadController.class,
        MediaDownloadService.class
})
@EnableAutoConfiguration
@EnableMongoRepositories
@Testcontainers
public class MediaDownloadControllerTest {

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
    DownloadedMediaRepository repository;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @Test
    @DisplayName("Check that the endpoint can retrieve downloads history from service")
    public void testCanRetrieveDownloadHistory() {
        String name = "name";
        long size = 1L;
        int year = 2012;
        int month = 10;
        int day = 1;
        int hour = 9;
        int minute = 33;
        int sec = 44;
        LocalDateTime date = LocalDateTime.of(year, month, day, hour, minute, sec);

        DownloadedMedia media = new DownloadedMedia();
        media.fileName = name;
        media.fileSize = size;
        media.dateDownloaded = date;

        repository.save(media);

        given()
                .contentType(ContentType.JSON)
                .when().get("/api/v1/media-downloads?year=" + year + "&month=" + month + "&day=" + day)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].fileName", is(name))
                .body("[0].fileSize", is((int) size))
                .body("[0].dateDownloaded", is(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    }
}