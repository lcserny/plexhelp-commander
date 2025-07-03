package net.cserny.command;

import net.cserny.generated.CommandResponse;
import net.cserny.generated.Status;

public interface Command {

    CommandResponse EMPTY = new CommandResponse().status(Status.SUCCESS);

    CommandResponse execute(String[] params);
    String name();
}
