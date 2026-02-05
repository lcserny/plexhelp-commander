package net.cserny.core.command.restart;

import net.cserny.core.command.AbstractOSCommand;
import net.cserny.core.command.CommandRunner;
import net.cserny.config.ServerCommandProperties;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RestartCommand extends AbstractOSCommand {

    private static final String NAME = "reboot";

    public RestartCommand(ServerCommandProperties properties,
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
        return List.of("systemctl", "reboot");
    }

    @Override
    protected List<String> produceCommandWindows(String[] params) {
        return List.of(getSystem32Prefix() + "shutdown.exe", "-r", "-f");
    }
}
