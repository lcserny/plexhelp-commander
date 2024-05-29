package net.cserny.command;

import org.springframework.stereotype.Component;

@Component
public class TestCommand implements Command {

    public static final String TEST_COMMAND = "test";

    private boolean executed;

    @Override
    public CommandResponse execute(String[] params) {
        executed = true;
        return CommandResponse.EMPTY;
    }

    @Override
    public String name() {
        return TEST_COMMAND;
    }

    public boolean isExecuted() {
        return executed;
    }
}
