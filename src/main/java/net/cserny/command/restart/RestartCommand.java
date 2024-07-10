package net.cserny.command.restart;

import lombok.extern.slf4j.Slf4j;
import net.cserny.command.Command;
import net.cserny.command.CommandResponse;
import net.cserny.command.ServerCommandProperties;
import net.cserny.command.shutdown.ShutdownCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// TODO: impl seconds
@Slf4j
@Component
public class RestartCommand implements Command {

    public static final String NAME = "reboot";

    @Autowired
    private ServerCommandProperties properties;

    @Override
    public CommandResponse execute(String[] params) {
        List<String> paramsList = new ArrayList<>();
        paramsList.add(NAME);

        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("win")) {
            paramsList = paramsWindows(params);
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(paramsList.toArray(new String[0]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return CommandResponse.EMPTY;
    }

    private List<String> paramsWindows(String[] params) {
        List<String> paramsList = new ArrayList<>();
        paramsList.add(getWinCommand());
        paramsList.add("-r");
        paramsList.add("-t");
        paramsList.add("0");
        return paramsList;
    }

    private String getWinCommand() {
        if (properties.isWsl() && properties.getWslOverrides().containsKey(ShutdownCommand.NAME)) {
            String override = properties.getWslOverrides().get(ShutdownCommand.NAME);
            log.info("Using WSL override {}", override);
            return override;
        }
        return ShutdownCommand.NAME;
    }

    @Override
    public String name() {
        return NAME;
    }
}
