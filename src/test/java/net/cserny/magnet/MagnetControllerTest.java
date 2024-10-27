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

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
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
                .body("content", hasSize(1));
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