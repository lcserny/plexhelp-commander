package net.cserny.command;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {
        CommandController.class,
        LocalCommandService.class,
        TestCommand.class
})
@EnableAutoConfiguration
public class CommandControllerTest {

    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @Test
    @DisplayName("Check that the endpoint can run a command through service")
    public void testCanExecuteLocalCommand() {
        CommandRequest request = new CommandRequest(TestCommand.TEST_COMMAND, null);
        
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/commands")
                .then()
                .statusCode(200)
                .body("status", is("SUCCESS"));
    }
}