package net.cserny.command;

public interface Command {

    CommandResponse execute(Param[] params);

    String name();
}
