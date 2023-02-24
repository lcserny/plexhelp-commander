package net.cserny.command;

import javax.enterprise.context.Dependent;

@Dependent
public class PrintCommand implements Command {

    public static final String NAME = "print";

    @Override
    public void execute() {
        System.out.println("Printing something");
    }

    @Override
    public String name() {
        return NAME;
    }
}
