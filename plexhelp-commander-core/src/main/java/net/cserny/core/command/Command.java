package net.cserny.core.command;

import net.cserny.core.command.CommandRunner.CommandResult;
import java.util.Optional;

public interface Command {

    Optional<CommandResult> execute(String[] params) throws Exception;
    String name();
}
