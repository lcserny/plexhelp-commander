package net.cserny.core.command.restart;

import net.cserny.core.command.AbstractOSCommand;
import net.cserny.core.command.CommandRunner;
import net.cserny.config.ServerCommandProperties;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RestartCommand extends AbstractOSCommand<Void> {

    public RestartCommand(ServerCommandProperties properties,
                          TaskScheduler taskScheduler,
                          CommandRunner commandRunner) {
        super(properties, taskScheduler, commandRunner);
    }

    @Override
    public CommandName name() {
        return CommandName.REBOOT;
    }

    @Override
    protected List<String> produceCommandLinux(String[] params) {
        return List.of("systemctl", "reboot");
    }

    @Override
    protected Void adaptOutput(String output) {
        return null;
    }

    @Override
    protected List<String> produceCommandWindows(String[] params) {
        return List.of(getSystem32Prefix() + "shutdown.exe", "-r", "-f");
    }
}
