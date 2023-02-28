package net.cserny.command;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Singleton
public class CommandTestBeans {

    @Produces
    public Command testCommand() {
        return new TestCommand();
    }
}
