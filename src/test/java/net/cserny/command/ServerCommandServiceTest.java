package net.cserny.command;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.MongoCloudTestSetup;
import net.cserny.command.shutdown.CommandResponse;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@Testcontainers
@QuarkusTestResource(MongoCloudTestSetup.class)
public class ServerCommandServiceTest {

    private static final Logger LOGGER = Logger.getLogger(ServerCommandServiceTest.class);

    private static final String TEST_COMMAND = "test";
    private static boolean testCommandExecuted = false;

    @Inject
    ServerCommandService service;

    @Inject
    ServerCommandRepository repository;

    @Inject
    ServerCommandConfig config;

    @Produces
    public Command testCommand() {
        return new Command() {
            @Override
            public CommandResponse execute() {
                testCommandExecuted = true;
                return CommandResponse.EMPTY;
            }

            @Override
            public String name() {
                return TEST_COMMAND;
            }
        };
    }

    @Test
    @DisplayName("Check that commands are executed ok from scheduled job")
    public void checkScheduledJobExecutesCommandsOk() {
        assertFalse(testCommandExecuted);

        service.initServerCommand();
        repository.getByServerName(config.name()).ifPresent(serverCommand -> {
            serverCommand.actionsPending = List.of(TEST_COMMAND);
            repository.updateServerCommand(serverCommand);
            LOGGER.info("Updating test command " + config.name() + " with command " + TEST_COMMAND);
        });
        service.startListeningForActions();

        assertTrue(testCommandExecuted);
    }
}
