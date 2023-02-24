package net.cserny.command.shutdown;

import net.cserny.command.Command;

import javax.enterprise.context.Dependent;

@Dependent
public class ShutdownCommand implements Command {

    @Override
    public void execute() {
        // TODO
        System.out.println("Shutting down");
    }
}
