package net.cserny.command;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import net.cserny.MongoTestConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(classes = {
        ServerCommandService.class,
        MongoTestConfiguration.class,
        ServerCommandRepository.class,
        TestCommand.class,
        ServerCommandProperties.class
})
@DataMongoTest(properties = {
        "server.command.name=test-server",
        "server.command.listen-cron=disabled"
})
@Testcontainers
public class ServerCommandServiceTest {

    private static final Logger LOGGER = Logger.getLogger(ServerCommandServiceTest.class);

    @Autowired
    ServerCommandService service;

    @Autowired
    ServerCommandRepository repository;

    @Autowired
    ServerCommandProperties config;

    @Autowired
    TestCommand testCommand;

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
