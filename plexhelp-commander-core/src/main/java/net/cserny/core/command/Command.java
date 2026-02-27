package net.cserny.core.command;

import net.cserny.api.dto.CommandResult;

import java.util.Optional;

public interface Command<T> {

    Optional<CommandResult<T>> execute(String[] params) throws Exception;
    String name();
}
