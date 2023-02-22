package net.cserny.download;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.MongoTestSetup;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@Testcontainers
@QuarkusTestResource(MongoTestSetup.class)
public class DownloadHistoryEndpointTest {

    @Inject
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
        LocalDateTime date = LocalDateTime.of(year, month, day, hour, minute, sec);

        DownloadedMedia media = new DownloadedMedia();
        media.fileName = name;
        media.fileSize = size;
        media.dateDownloaded = date;

        repository.persist(media);

        given()
                .when().get("/api/v1/downloads/completed/" + year + "/" + month + "/" + day)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].fileName", is(name))
                .body("[0].fileSize", is((int) size))
                .body("[0].dateDownloaded", is(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    }

}