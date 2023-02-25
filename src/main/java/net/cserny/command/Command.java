package net.cserny.command;

import net.cserny.command.shutdown.CommandResponse;

public interface Command {

    CommandResponse execute();

    String name();
}
