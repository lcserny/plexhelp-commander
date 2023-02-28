package net.cserny.command;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.MongoCloudTestSetup;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@Testcontainers
@QuarkusTestResource(MongoCloudTestSetup.class)
public class ServerCommandServiceTest {

    private static final Logger LOGGER = Logger.getLogger(ServerCommandServiceTest.class);

    @Inject
    ServerCommandService service;

    @Inject
    ServerCommandRepository repository;

    @Inject
    ServerCommandConfig config;

    @Inject
    TestCommand testCommand;

    @Test
    @DisplayName("Check that commands are executed ok from scheduled job")
    public void checkScheduledJobExecutesCommandsOk() {
        assertFalse(testCommand.isExecuted());

        service.initServerCommand();
        repository.getByServerName(config.name()).ifPresent(serverCommand -> {
            serverCommand.actionsPending = List.of(TestCommand.TEST_COMMAND);
            repository.updateServerCommand(serverCommand);
            LOGGER.info("Updating test command " + config.name() + " with command " + TestCommand.TEST_COMMAND);
        });
        service.startListeningForActions();

        assertTrue(testCommand.isExecuted());
    }
}
