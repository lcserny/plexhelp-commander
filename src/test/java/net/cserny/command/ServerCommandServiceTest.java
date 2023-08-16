package net.cserny.command;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(value = {
        "server.command.name=test-server",
        "server.command.listen-cron=disabled"
})
@ContextConfiguration(classes = {
        ServerCommandService.class,
        ServerCommandRepository.class,
        TestCommand.class,
        ServerCommandConfig.class
})
@EnableAutoConfiguration
@EnableMongoRepositories
@Testcontainers
public class ServerCommandServiceTest {

    @Container
    public static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:5.0");

    @DynamicPropertySource
    public static void qTorrentProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoContainer.getConnectionString());
    }

    private static final Logger LOGGER = Logger.getLogger(ServerCommandServiceTest.class);

    @Autowired
    ServerCommandService service;

    @Autowired
    ServerCommandRepository repository;

    @Autowired
    ServerCommandConfig config;

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
