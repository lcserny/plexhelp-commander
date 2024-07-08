package net.cserny.command.restart;

import net.cserny.command.Command;
import net.cserny.command.CommandResponse;
import net.cserny.command.LinuxEnvConditional;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// TODO: impl seconds
@Component
@Conditional(LinuxEnvConditional.class)
public class RestartLinuxCommand implements Command {

    public static final String NAME = "reboot";

    @Override
    public CommandResponse execute(String[] params) {
        List<String> paramsList = new ArrayList<>();
        paramsList.add(NAME);
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(paramsList.toArray(new String[0]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return CommandResponse.EMPTY;
    }

    @Override
    public String name() {
        return NAME;
    }
}
