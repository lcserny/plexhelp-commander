package net.cserny.command;

import io.restassured.http.ContentType;
import net.cserny.IntegrationTest;
import net.cserny.generated.CommandRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class CommandControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Check that the endpoint can run a command through service")
    public void testCanExecuteLocalCommand() {
        CommandRequest request = new CommandRequest().name(TestCommand.TEST_COMMAND);
        
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/commands")
                .then()
                .statusCode(200)
                .body("status", is("SUCCESS"));
    }
}