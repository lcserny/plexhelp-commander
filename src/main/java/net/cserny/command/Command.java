package net.cserny.command;

public interface Command {

    CommandResponse execute(String[] params);

    String name();
}
