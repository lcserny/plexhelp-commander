package net.cserny.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {
    LocalCommandService.class,
    TestCommand.class
})
public class LocalCommandServiceTest {
    
    @Autowired
    LocalCommandService commandService;

    @Autowired
    TestCommand testCommand;

    @Test
    @DisplayName("command service can execute test command successfully")
    public void serviceCanExecuteCommand() {
        assertFalse(testCommand.isExecuted());

        commandService.execute(TestCommand.TEST_COMMAND, null);

        assertTrue(testCommand.isExecuted());
    }
}
