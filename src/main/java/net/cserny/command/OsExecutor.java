package net.cserny.command;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public interface OsExecutor {

    ExecutionResponse execute(String command) throws Exception;

    default ExecutionResponse execute(List<String> commands) throws Exception {
        return execute(String.join(" ", commands));
    }

    record ExecutionResponse(int exitCode, String response) {

        @Override
        public String response() {
            if (StringUtils.isEmpty(response)) {
                return "<empty>";
            }
            return response;
        }
    }
}
