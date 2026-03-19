package net.cserny.core.command.sleep;

import net.cserny.core.command.AbstractOSCommand;
import net.cserny.core.command.CommandRunner;
import net.cserny.config.ServerCommandProperties;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SleepCommand extends AbstractOSCommand<Void> {

    public SleepCommand(ServerCommandProperties properties,
                        TaskScheduler taskScheduler,
                        CommandRunner commandRunner) {
        super(properties, taskScheduler, commandRunner);
    }

    @Override
    public CommandName name() {
        return CommandName.SLEEP;
    }

    @Override
    protected List<String> produceCommandLinux(String[] params) {
        return List.of("systemctl", "suspend");
    }

    @Override
    protected Void adaptImmediateOutput(String output) {
        return null;
    }

    @Override
    protected List<String> produceCommandWindows(String[] params) {
        return List.of(getSystem32Prefix() + "Rundll32.exe", "powrprof.dll,SetSuspendState", "0,1,0");
    }
}
