package net.cserny.download;

import io.restassured.http.ContentType;
import net.cserny.IntegrationTest;
import net.cserny.download.internal.DownloadedMediaRepository;
import net.cserny.generated.SearchDownloadedMedia;
import net.cserny.generated.SearchDownloadedMediaDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;

public class MediaDownloadControllerTest extends IntegrationTest {

    @Autowired
    DownloadedMediaRepository repository;

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
                .body("[0].dateDownloaded", equalTo((float) date.getEpochSecond()));
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