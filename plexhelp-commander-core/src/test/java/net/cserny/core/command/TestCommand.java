package net.cserny.core.command;

import lombok.Getter;
import net.cserny.api.dto.CommandResult;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Getter
@Component
public class TestCommand implements Command<String> {

    public static final String TEST_COMMAND = "test";

    private boolean executed;

    public void reset() {
        executed = false;
    }

    @Override
    public Optional<CommandResult<String>> execute(String[] params) {
        executed = true;
        return Optional.of(new CommandResult<>(true, false, ""));
    }

    @Override
    public String name() {
        return TEST_COMMAND;
    }
}
