package net.cserny.command.shutdown;

import net.cserny.command.Command;

import javax.enterprise.context.Dependent;

@Dependent
public class ShutdownCommand implements Command {

    public static final String NAME = "shutdown";

    @Override
    public void execute() {
        // TODO
        System.out.println("Shutting down");
    }

    @Override
    public String name() {
        return NAME;
    }
}
