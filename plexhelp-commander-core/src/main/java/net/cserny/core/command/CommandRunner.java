package net.cserny.core.command;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public interface CommandRunner {

    CommandResult ERROR_RESULT = new CommandResult(1, null);

    CommandResult run(String command) throws Exception;

    default CommandResult run(List<String> commands) throws Exception {
        return run(String.join(" ", commands));
    }

    record CommandResult(int exitCode, String response) {

        public boolean isSuccess() {
            return exitCode == 0;
        }

        @Override
        public String response() {
            if (StringUtils.isEmpty(response)) {
                return "<empty>";
            }
            return response;
        }
    }
}
