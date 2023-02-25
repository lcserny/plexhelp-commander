package net.cserny.command.shutdown;

import net.cserny.command.Command;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import java.io.IOException;

@ApplicationScoped
public class ShutdownCommand implements Command {

    public static final String NAME = "shutdown";

    // TODO: impl params passing from Cloud DB to here for seconds or other data
    @Override
    public CommandResponse execute() {
        Runtime runtime = Runtime.getRuntime();
        String os = System.getProperty("os.name");
        if (os.contains("win")) {
            shutdownWindows(runtime);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            shutdownUnix(runtime);
        }
        return CommandResponse.EMPTY;
    }

    private void shutdownUnix(Runtime runtime) {
        try {
            runtime.exec("shutdown now");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void shutdownWindows(Runtime runtime) {
        try {
            runtime.exec("shutdown -s");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String name() {
        return NAME;
    }
}
