package net.cserny.core.command;

import java.util.Optional;

public interface Command<T> {

    Optional<CommandResult<T>> execute(String[] params) throws Exception;
    String name();

    record CommandResult<R>(boolean success, boolean delayed, R result) {}
}
