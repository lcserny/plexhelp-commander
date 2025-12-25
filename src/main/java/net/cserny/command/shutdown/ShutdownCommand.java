package net.cserny.command.shutdown;

import lombok.extern.slf4j.Slf4j;
import net.cserny.command.AbstractOSCommand;
import net.cserny.command.ServerCommandProperties;
import net.cserny.command.SshExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ShutdownCommand extends AbstractOSCommand {

    private static final String NAME = "shutdown";

    public ShutdownCommand(ServerCommandProperties properties,
                           TaskExecutor taskExecutor,
                           TaskScheduler taskScheduler,
                           SshExecutor sshExecutor) {
        super(properties, taskExecutor, taskScheduler, sshExecutor);
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
        return List.of(getCommandPrefix() + "shutdown.exe", "-s", "-f");
    }
}
