package net.cserny.command;

import net.cserny.command.shutdown.CommandResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;

@ApplicationScoped
public class PrintCommand implements Command {

    public static final String NAME = "print";

    @Override
    public CommandResponse execute() {
        System.out.println("Printing something");
        return CommandResponse.EMPTY;
    }

    @Override
    public String name() {
        return NAME;
    }
}
