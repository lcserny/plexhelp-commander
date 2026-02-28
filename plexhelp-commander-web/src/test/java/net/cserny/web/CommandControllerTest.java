package net.cserny.web;

import io.restassured.http.ContentType;
import net.cserny.generated.CommandRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static net.cserny.api.Command.CommandName.TEST;
import static org.hamcrest.CoreMatchers.is;

public class CommandControllerTest extends WebIntegrationTest {

    @Test
    @DisplayName("Check that the endpoint can run a command through service")
    public void testCanExecuteLocalCommand() {
        CommandRequest request = new CommandRequest().name(TEST.getValue());
        
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/commands")
                .then()
                .statusCode(200)
                .body("status", is("SUCCESS"));
    }
}