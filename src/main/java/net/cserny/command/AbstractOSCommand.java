package net.cserny.command;

import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.CommandResponse;
import net.cserny.generated.Status;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Slf4j
public abstract class AbstractOSCommand implements Command {

    @Autowired
    protected ServerCommandProperties properties;

    @Override
    public CommandResponse execute(String[] params) {
        try {
            Runtime runtime = Runtime.getRuntime();
            if (SystemUtils.IS_OS_WINDOWS || properties.isWsl()) {
                executeInternalWindows(runtime, params);
            } else if (SystemUtils.IS_OS_LINUX) {
                executeInternalLinux(runtime, params);
            } else {
                throw new RuntimeException("Unsupported operating system: " + SystemUtils.OS_NAME);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new CommandResponse(Status.FAILED);
        }

        return Command.EMPTY;
    }

    protected abstract void executeInternalWindows(Runtime runtime, String[] params) throws IOException;

    protected abstract void executeInternalLinux(Runtime runtime, String[] params) throws IOException;
}
