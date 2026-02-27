package net.cserny.core.command;

import java.util.List;

public interface CommandRunner {

    CommandOutput run(String command) throws Exception;

    default CommandOutput run(List<String> commands) throws Exception {
        return run(String.join(" ", commands));
    }

    record CommandOutput(int exitCode, String response) {}
}
