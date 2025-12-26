package net.cserny.command.sleep;

import lombok.extern.slf4j.Slf4j;
import net.cserny.command.AbstractOSCommand;
import net.cserny.command.OsExecutor;
import net.cserny.command.ServerCommandProperties;
import net.cserny.command.SshExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SleepCommand extends AbstractOSCommand {

    private static final String NAME = "sleep";

    public SleepCommand(ServerCommandProperties properties,
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
        return List.of("systemctl", "suspend");
    }

    @Override
    protected List<String> produceCommandWindows(String[] params) {
        return List.of(getCommandPrefix() + "Rundll32.exe", "powrprof.dll,SetSuspendState", "0,1,0");
    }
}
