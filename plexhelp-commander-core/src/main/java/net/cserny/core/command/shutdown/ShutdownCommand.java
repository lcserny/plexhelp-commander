package net.cserny.core.command.shutdown;

import net.cserny.core.command.AbstractOSCommand;
import net.cserny.core.command.CommandRunner;
import net.cserny.config.ServerCommandProperties;
import net.cserny.core.command.CommandRunner.CommandOutput;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShutdownCommand extends AbstractOSCommand<Void> {

    private static final String NAME = "shutdown";

    public ShutdownCommand(ServerCommandProperties properties,
                           TaskScheduler taskScheduler,
                           CommandRunner commandRunner) {
        super(properties, taskScheduler, commandRunner);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected List<String> produceCommandLinux(String[] params) {
        return List.of("systemctl", "poweroff");
    }

    @Override
    protected Void adaptOutput(String output) {
        return null;
    }

    @Override
    protected List<String> produceCommandWindows(String[] params) {
        return List.of(getSystem32Prefix() + "shutdown.exe", "-s", "-f");
    }
}
