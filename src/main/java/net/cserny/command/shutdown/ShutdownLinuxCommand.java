package net.cserny.command.shutdown;

import net.cserny.command.Command;
import net.cserny.command.CommandResponse;
import net.cserny.command.LinuxEnvConditional;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Conditional(LinuxEnvConditional.class)
public class ShutdownLinuxCommand implements Command {

    public static final String NAME = "shutdown";

    @Override
    public CommandResponse execute(String[] params) {
        List<String> paramsList = new ArrayList<>();
        paramsList.add(NAME);
        if (params == null || params.length == 0) {
            paramsList.add("now");
        } else {
            Collections.addAll(paramsList, params);
        }

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
