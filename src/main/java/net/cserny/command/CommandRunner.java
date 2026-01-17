package net.cserny.command;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public interface CommandRunner {

    CommandResponse run(String command) throws Exception;

    default CommandResponse run(List<String> commands) throws Exception {
        return run(String.join(" ", commands));
    }

    record CommandResponse(int exitCode, String response) {

        @Override
        public String response() {
            if (StringUtils.isEmpty(response)) {
                return "<empty>";
            }
            return response;
        }
    }
}
