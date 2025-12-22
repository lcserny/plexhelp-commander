package net.cserny.command.restart;

import lombok.extern.slf4j.Slf4j;
import net.cserny.command.AbstractOSCommand;
import net.cserny.command.ServerCommandProperties;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class RestartCommand extends AbstractOSCommand {

    private static final String NAME = "reboot";

    public RestartCommand(ServerCommandProperties properties, TaskExecutor taskExecutor, TaskScheduler taskScheduler) {
        super(properties, taskExecutor, taskScheduler);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected List<String> produceCommandLinux(String[] params) {
        return List.of("reboot", "-r", "now");
    }

    @Override
    protected List<String> produceCommandWindows(String[] params) {
        return List.of(getCommandPrefix() + "shutdown.exe", "-r", "-f");
    }
}
