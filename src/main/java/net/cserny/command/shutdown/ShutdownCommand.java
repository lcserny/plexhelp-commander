package net.cserny.command.shutdown;

import lombok.extern.slf4j.Slf4j;
import net.cserny.command.Command;
import net.cserny.command.CommandResponse;
import net.cserny.command.ServerCommandProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ShutdownCommand implements Command {

    public static final String NAME = "shutdown";

    @Autowired
    private ServerCommandProperties properties;

    @Override
    public CommandResponse execute(String[] params) {
        List<String> paramsList = new ArrayList<>();
        paramsList.add(getCommand());

        if (params == null || params.length == 0) {
            paramsList.add("-s");
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

    private String getCommand() {
        if (properties.isWsl() && properties.getWslOverrides().containsKey(NAME)) {
            String override = properties.getWslOverrides().get(NAME);
            log.info("Using WSL override {}", override);
            return override;
        }
        return NAME;
    }

    @Override
    public String name() {
        return NAME;
    }
}
