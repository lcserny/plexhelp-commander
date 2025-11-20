package net.cserny.command;

import net.cserny.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalCommandServiceTest extends IntegrationTest {
    
    @Autowired
    LocalCommandService commandService;

    @Autowired
    TestCommand testCommand;

    @BeforeEach
    public void setUp() {
        testCommand.reset();
    }

    @Test
    @DisplayName("command service can execute test command successfully")
    public void serviceCanExecuteCommand() {
        assertFalse(testCommand.isExecuted());

        commandService.execute(TestCommand.TEST_COMMAND, null);

        assertTrue(testCommand.isExecuted());
    }
}
