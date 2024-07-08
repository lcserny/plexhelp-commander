package net.cserny.command.restart;

import net.cserny.command.Command;
import net.cserny.command.CommandResponse;
import net.cserny.command.ServerCommandProperties;
import net.cserny.command.WindowsEnvConditional;
import net.cserny.command.shutdown.ShutdownWinCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// TODO: impl seconds
@Component
@Conditional(WindowsEnvConditional.class)
public class RestartWinCommand implements Command {

    public static final String NAME = "reboot";

    @Autowired
    private ServerCommandProperties properties;

    @Override
    public CommandResponse execute(String[] params) {
        List<String> paramsList = new ArrayList<>();
        paramsList.add(getCommand());
        paramsList.add("-r");
        paramsList.add("-t");
        paramsList.add("0");
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(paramsList.toArray(new String[0]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return CommandResponse.EMPTY;
    }

    private String getCommand() {
        if (properties.isWsl() && properties.getWslOverrides().containsKey(ShutdownWinCommand.NAME)) {
            return properties.getWslOverrides().get(ShutdownWinCommand.NAME);
        }
        return ShutdownWinCommand.NAME;
    }

    @Override
    public String name() {
        return NAME;
    }
}
