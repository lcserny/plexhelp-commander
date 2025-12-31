package net.cserny.command.shutdown;

import net.cserny.command.AbstractOSCommand;
import net.cserny.command.OsExecutor;
import net.cserny.command.ServerCommandProperties;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShutdownCommand extends AbstractOSCommand {

    private static final String NAME = "shutdown";

    public ShutdownCommand(ServerCommandProperties properties,
                           TaskScheduler taskScheduler,
                           OsExecutor osExecutor) {
        super(properties, taskScheduler, osExecutor);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected List<String> produceCommandLinux(String[] params) {
        return List.of("shutdown", "now");
    }

    @Override
    protected List<String> produceCommandWindows(String[] params) {
        return List.of(getSystem32Prefix() + "shutdown.exe", "-s", "-f");
    }
}
