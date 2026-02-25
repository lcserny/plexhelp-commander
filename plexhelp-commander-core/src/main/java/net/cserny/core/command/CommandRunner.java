package net.cserny.core.command;

import java.util.List;

public interface CommandRunner {

    CommandResult run(String command) throws Exception;

    default CommandResult run(List<String> commands) throws Exception {
        return run(String.join(" ", commands));
    }

    record CommandResult(int exitCode, String response) {}
}
