package net.cserny.core.command;

import lombok.Getter;
import net.cserny.api.Command;
import net.cserny.api.dto.CommandResult;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Getter
@Component
public class TestCommand implements Command<String> {

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
    public CommandName name() {
        return CommandName.TEST;
    }
}
