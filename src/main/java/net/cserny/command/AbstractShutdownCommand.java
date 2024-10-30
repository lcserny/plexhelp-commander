package net.cserny.command;

import lombok.extern.slf4j.Slf4j;
import net.cserny.command.shutdown.ShutdownCommand;
import net.cserny.generated.CommandResponse;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class AbstractShutdownCommand implements Command {

    @Autowired
    protected ServerCommandProperties properties;

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

        return Command.EMPTY;
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

    protected String getCommand() {
        if (properties.isWsl() && properties.getWslOverrides().containsKey(ShutdownCommand.NAME)) {
            String override = properties.getWslOverrides().get(ShutdownCommand.NAME);
            log.info("Using WSL override {}", override);
            return override;
        }
        return ShutdownCommand.NAME;
    }

    protected abstract void appendWindowsParams(List<String> outParams, String[] inParams);

    protected abstract void appendLinuxParams(List<String> outParams, String[] inParams);
}
