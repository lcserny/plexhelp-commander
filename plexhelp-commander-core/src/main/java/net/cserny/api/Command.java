package net.cserny.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.cserny.api.dto.CommandResult;

import java.util.Optional;

public interface Command<T> {

    Optional<CommandResult<T>> execute(String[] params) throws Exception;

    CommandName name();

    @Getter
    @RequiredArgsConstructor
    enum CommandName {

        TEST("test"),
        SCAN_SUBS("ffmpeg-scan-subs"),
        REDUCE_SUBS("ffmpeg-reduce-subs"),
        SHUTDOWN("shutdown"),
        REBOOT("reboot"),
        SLEEP("sleep"),
        RESTART_SERVICE("restart-service");

        private final String value;

        public static CommandName from(String value) {
            for (CommandName commandName : CommandName.values()) {
                if (commandName.getValue().equals(value)) {
                    return commandName;
                }
            }
            throw new IllegalArgumentException(value + " is not a valid command name value");
        }
    }
}
