package net.cserny.command;

import net.cserny.IntegrationTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerCommandServiceTest extends IntegrationTest {

    private static final Logger LOGGER = Logger.getLogger(ServerCommandServiceTest.class);

    @Autowired
    ServerCommandService service;

    @Autowired
    ServerCommandRepository repository;

    @Autowired
    ServerCommandProperties config;

    @Autowired
    TestCommand testCommand;

    @BeforeEach
    public void setUp() {
        testCommand.reset();
    }

    @Test
    @DisplayName("Check that commands are executed ok from scheduled job")
    public void checkScheduledJobExecutesCommandsOk() {
        assertFalse(testCommand.isExecuted());

        service.initServerCommand();
        repository.getByServerName(config.getName()).ifPresent(serverCommand -> {
            serverCommand.actionsPending = List.of(TestCommand.TEST_COMMAND);
            repository.save(serverCommand);
            LOGGER.info("Updating test command " + config.getName() + " with command " + TestCommand.TEST_COMMAND);
        });
        service.startListeningForActions();

        assertTrue(testCommand.isExecuted());
    }
}
