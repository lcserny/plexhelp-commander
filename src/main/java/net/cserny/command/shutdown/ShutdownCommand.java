package net.cserny.command.shutdown;

import net.cserny.command.Command;
import net.cserny.command.CommandResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ShutdownCommand implements Command {

    public static final String NAME = "shutdown";

    // TODO: impl params for seconds
    @Override
    public CommandResponse execute(String[] params) {
        Runtime runtime = Runtime.getRuntime();
        String os = System.getProperty("os.name");
        if (os.contains("win")) {
            shutdownWindows(runtime, params);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            shutdownUnix(runtime, params);
        }
        return CommandResponse.EMPTY;
    }

    private void shutdownUnix(Runtime runtime, String[] params) {
        try {
            if (params == null || params.length == 0) {
                params = new String[]{"now"};
            }
            runtime.exec(new String[]{"shutdown"}, params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void shutdownWindows(Runtime runtime, String[] params) {
        try {
            if (params == null || params.length == 0) {
                params = new String[]{"-s"};
            }
            runtime.exec(new String[]{"shutdown"}, params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String name() {
        return NAME;
    }
}
