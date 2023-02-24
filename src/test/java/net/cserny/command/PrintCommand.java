package net.cserny.command;

import javax.enterprise.context.Dependent;

@Dependent
public class PrintCommand implements Command {

    @Override
    public void execute() {
        System.out.println("Printing something");
    }
}
