package net.cserny.command.sleep;

import net.cserny.command.AbstractOSCommand;
import net.cserny.command.CommandRunner;
import net.cserny.command.ServerCommandProperties;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SleepCommand extends AbstractOSCommand {

    private static final String NAME = "sleep";

    public SleepCommand(ServerCommandProperties properties,
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
        return List.of("systemctl", "suspend");
    }

    @Override
    protected List<String> produceCommandWindows(String[] params) {
        return List.of(getSystem32Prefix() + "Rundll32.exe", "powrprof.dll,SetSuspendState", "0,1,0");
    }
}
