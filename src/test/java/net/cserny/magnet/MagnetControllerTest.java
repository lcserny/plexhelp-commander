package net.cserny.magnet;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import net.cserny.MongoTestConfiguration;
import net.cserny.QTorrentTestConfiguration;
import net.cserny.qtorrent.QTorrentRestClient;
import net.cserny.qtorrent.TorrentProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        MagnetController.class,
        MagnetService.class,
        MagnetRepository.class,
        QTorrentRestClient.class,
        RestTemplate.class,
        TorrentProperties.class,
        QTorrentTestConfiguration.class,
        MongoTestConfiguration.class
})
@EnableAutoConfiguration
@AutoConfigureDataMongo
@Testcontainers
class MagnetControllerTest {

    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @Autowired
    MagnetRepository repository;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @Test
    @DisplayName("GET on /magnets should return all magnets from db paginated")
    public void retrieveAllMagnets() {
        this.repository.save(new Magnet());

        given()
                .contentType(ContentType.TEXT)
                .when().get("/api/v1/magnets")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(not(0)));
    }

    @Test
    @DisplayName("GET on /magnets should return filtered magnets from db paginated")
    public void retrieveFilteredMagnets() {
        Magnet magnetOne = new Magnet();
        magnetOne.setName("one");
        Magnet magnetTwo = new Magnet();
        magnetTwo.setName("two");
        this.repository.saveAll(List.of(magnetOne, magnetTwo));

        given()
                .contentType(ContentType.TEXT)
                .when().get(format("/api/v1/magnets?name=%s", "one"))
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(1));

        given()
                .contentType(ContentType.TEXT)
                .when().get(format("/api/v1/magnets?name=%s", "two"))
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(1));

        // FIXME: these two fail because TorrentsServiceTest interferes with the db data in parallel
//        given()
//                .contentType(ContentType.TEXT)
//                .when().get(format("/api/v1/magnets?page=%d&size=%d&sort=%s", 0, 1, "name"))
//                .then()
//                .statusCode(HttpStatus.OK.value())
//                .log().body()
//                .body("content", hasSize(1))
//                .body("content[0].name", containsString("one"));
//
//        given()
//                .contentType(ContentType.TEXT)
//                .when().get(format("/api/v1/magnets?page=%d&size=%d&sort=%s", 0, 2, "name,ASC"))
//                .then()
//                .statusCode(HttpStatus.OK.value())
//                .log().body()
//                .body("content", hasSize(2))
//                .body("content[0].name", containsString("one"))
//                .body("content[1].name", containsString("two"));

        given()
                .contentType(ContentType.TEXT)
                .when().get(format("/api/v1/magnets?page=%d&size=%d&sort=%s&sort=%s", 0, 2, "name,DESC", "dateAdded,ASC"))
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(2))
                .body("content[0].name", containsString("two"))
                .body("content[1].name", containsString("one"));
    }

    @Test
    @DisplayName("POST on /magnets should add a new magnet to torrent client and db")
    public void addMagnet() {
        var hash = "abc";
        var name = "myName";
        var magnetLink = format("magnet:?xt=urn:btih:%s&dn=%s&tr=whatever", hash, name);

        given()
                .contentType(ContentType.TEXT)
                .body(magnetLink)
                .when().post("/api/v1/magnets")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("hash", is(hash))
                .body("name", is(name));
    }
}