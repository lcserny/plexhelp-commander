package net.cserny.command;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.MongoCloudTestSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;

@QuarkusTest
@Testcontainers
@QuarkusTestResource(MongoCloudTestSetup.class)
public class ServerCommandServiceTest {

    @Inject
    ServerCommandService service;

    @Test
    @DisplayName("Starts ok")
    public void startOk() {
        // add in test mongo instance, the print command
        // run the startListeningForActions from service

        System.out.println(service.commands);
    }
}
