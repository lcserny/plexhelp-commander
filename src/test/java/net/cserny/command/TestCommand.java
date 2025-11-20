package net.cserny.command;

import lombok.Getter;
import net.cserny.generated.CommandResponse;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Getter
@Component
public class TestCommand implements Command {

    public static final String TEST_COMMAND = "test";

    private boolean executed;

    public void reset() {
        executed = false;
    }

    @Override
    public CommandResponse execute(String[] params) {
        executed = true;
        return Command.EMPTY;
    }

    @Override
    public String name() {
        return TEST_COMMAND;
    }
}
