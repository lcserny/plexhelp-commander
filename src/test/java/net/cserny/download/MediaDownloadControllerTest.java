package net.cserny.download;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import net.cserny.MongoTestConfiguration;

import net.cserny.generated.SearchDownloadedMedia;
import net.cserny.generated.SearchDownloadedMediaDate;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

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
        DownloadedMediaRepository.class,
        InternalDownloadedMediaRepository.class,
        MongoTestConfiguration.class,
        MediaDownloadController.class,
        MediaDownloadService.class
})
@EnableAutoConfiguration
@AutoConfigureDataMongo
@Testcontainers
public class MediaDownloadControllerTest {

    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

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
        Instant date = LocalDateTime.of(year, month, day, hour, minute, sec).atZone(ZoneOffset.UTC).toInstant();

        DownloadedMedia media = new DownloadedMedia();
        media.setFileName(name);
        media.setFileSize(size);
        media.setDateDownloaded(date);

        repository.save(media);

        SearchDownloadedMedia request = new SearchDownloadedMedia().date(new SearchDownloadedMediaDate(year, month, day));

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/media-downloads")
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].fileName", is(name))
                .body("[0].fileSize", is((int) size))
                .body("[0].dateDownloaded", is(DateTimeFormatter.ISO_INSTANT.format(date)));
    }

    @Test
    @DisplayName("Check that the endpoint fails with bad request when a required field is not sent")
    public void testInvalidRequest() {
        String name = "some other name for validation";
        long size = 2L;
        int year = 2011;
        int month = 10;
        int day = 1;
        int hour = 9;
        int minute = 33;
        int sec = 44;
        Instant date = LocalDateTime.of(year, month, day, hour, minute, sec).atZone(ZoneOffset.UTC).toInstant();

        DownloadedMedia media = new DownloadedMedia();
        media.setFileName(name);
        media.setFileSize(size);
        media.setDateDownloaded(date);

        repository.save(media);

        SearchDownloadedMedia request = new SearchDownloadedMedia().date(new SearchDownloadedMediaDate().year(year));

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/media-downloads")
                .then()
                .statusCode(400);
    }
}