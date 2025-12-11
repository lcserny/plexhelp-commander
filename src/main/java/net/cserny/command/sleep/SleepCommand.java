package net.cserny.command.sleep;

import lombok.extern.slf4j.Slf4j;
import net.cserny.command.AbstractOSCommand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
@Component
public class SleepCommand extends AbstractOSCommand {

    private static final String NAME = "sleep";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected Process executeInternalLinux(Runtime runtime, String[] params) throws IOException {
        if (params.length == 0) {
            return execWithoutDelay(runtime);
        } else if (params.length == 1 && StringUtils.isNumeric(params[0])) {
            int minutes = Integer.parseInt(params[0]);
            if (minutes == 0) {
                return execWithoutDelay(runtime);
            } else {
                return execWithDelay(runtime, minutes);
            }
        } else {
            log.info("Sleeping system with params: {}", Arrays.toString(params));
            throw new UnsupportedOperationException("Sleeping with random params not supported");
        }
    }

    private Process execWithoutDelay(Runtime runtime) throws IOException {
        log.info("Sleeping system without delay");
        return runtime.exec(new String[]{"systemctl", "suspend"});
    }

    private Process execWithDelay(Runtime runtime, int minutes) throws IOException {
        log.info("Sleeping system in {} minutes", minutes);
        return runtime.exec(new String[]{"sleep", minutes + "m" , "&&", "systemctl", "suspend"});
    }

    @Override
    protected Process executeInternalWindows(Runtime runtime, String[] params) throws IOException {
        throw new RuntimeException("Not implemented");
    }
}
