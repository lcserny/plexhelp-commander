package net.cserny.command.restart;

import net.cserny.command.Command;
import net.cserny.command.CommandResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestartCommand implements Command {

    public static final String NAME = "reboot";

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
            runtime.exec(new String[]{"reboot"});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void shutdownWindows(Runtime runtime, String[] params) {
        throw new RuntimeException("Reboot command not available for Windows OS");
    }

    @Override
    public String name() {
        return NAME;
    }
}
