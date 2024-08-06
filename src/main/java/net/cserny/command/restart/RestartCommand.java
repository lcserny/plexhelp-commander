package net.cserny.command.restart;

import lombok.extern.slf4j.Slf4j;
import net.cserny.command.Command;
import net.cserny.command.CommandResponse;
import net.cserny.command.ServerCommandProperties;
import net.cserny.command.shutdown.ShutdownCommand;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class RestartCommand implements Command {

    public static final String NAME = "reboot";

    @Autowired
    ServerCommandProperties properties;

    @Override
    public CommandResponse execute(String[] inParams) {
        validate();

        List<String> outParams = new ArrayList<>();
        outParams.add(getCommand());
        appendParams(outParams, inParams);

        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(outParams.toArray(new String[0]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return CommandResponse.EMPTY;
    }

    private void validate() {
        if (!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_LINUX) {
            throw new RuntimeException("Unsupported operating system: " + SystemUtils.OS_NAME);
        }
    }

    private void appendParams(List<String> outParams, String[] inParams) {
        if (SystemUtils.IS_OS_WINDOWS || properties.isWsl()) {
            appendWindowsParams(outParams, inParams);
        } else if (SystemUtils.IS_OS_LINUX) {
            appendLinuxParams(outParams, inParams);
        }
    }

    private void appendLinuxParams(List<String> outParams, String[] inParams) {
        if (inParams.length == 0) {
            outParams.add("-r");
            outParams.add("now");
        } else if (inParams.length == 1 && StringUtils.isNumeric(inParams[0])) {
            outParams.add("-r");
            outParams.add("+" + Integer.parseInt(inParams[0]));
        } else {
            Collections.addAll(outParams, inParams);
        }
    }

    private void appendWindowsParams(List<String> outParams, String[] inParams) {
        if (inParams.length == 0) {
            outParams.add("-r");
        } else if (inParams.length == 1 && StringUtils.isNumeric(inParams[0])) {
            outParams.add("-r");
            outParams.add("-t");
            outParams.add(String.valueOf(60 * Integer.parseInt(inParams[0])));
        } else {
            Collections.addAll(outParams, inParams);
        }
    }

    private String getCommand() {
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
