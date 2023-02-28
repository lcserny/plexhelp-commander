package net.cserny.command;

import net.cserny.command.shutdown.CommandResponse;

import javax.inject.Singleton;

@Singleton
public class TestCommand implements Command {

    public static final String TEST_COMMAND = "test";

    private boolean executed;

    @Override
    public CommandResponse execute() {
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
