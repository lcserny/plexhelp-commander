package net.cserny.core.command;

import lombok.Getter;
import net.cserny.core.command.CommandRunner.CommandResult;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Getter
@Component
public class TestCommand implements Command {

    private static final CommandResult success = new CommandResult(0, null);

    public static final String TEST_COMMAND = "test";

    private boolean executed;

    public void reset() {
        executed = false;
    }

    @Override
    public Optional<CommandResult> execute(String[] params) {
        executed = true;
        return Optional.of(success);
    }

    @Override
    public String name() {
        return TEST_COMMAND;
    }
}
