package net.cserny.command.restart;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.cserny.command.AbstractOSCommand;
import net.cserny.command.DummyProcess;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
@Component
@RequiredArgsConstructor
public class RestartDLNACommand extends AbstractOSCommand {

    private static final String NAME = "restart-dlna";

    private final TaskScheduler scheduler;

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
            log.info("Restarting DLNA Media Server with params: {}", Arrays.toString(params));
            throw new UnsupportedOperationException("Restarting DLNA Media Server with random params not supported");
        }
    }

    private String[] getStopCommandParts() {
        return new String[]{"systemctl", "--user", "stop", "ums"};
    }

    private String[] getStartCommandParts() {
        return new String[]{"systemctl", "--user", "start", "ums"};
    }

    @SneakyThrows
    private Process execWithoutDelay(Runtime runtime) throws IOException {
        log.info("Restarting DLNA Media Server without delay");
        Process stopProcess = runtime.exec(getStopCommandParts());
        int stopExitCode = stopProcess.waitFor();
        if (stopExitCode == 0) {
            return runtime.exec(getStartCommandParts());
        }
        return stopProcess;
    }

    private Process execWithDelay(Runtime runtime, int minutes) throws IOException {
        log.info("Restarting DLNA Media Server in {} minutes", minutes);
        scheduler.schedule(() -> {
            try {
                runtime.exec(getStopCommandParts()).waitFor();
                runtime.exec(getStartCommandParts()).waitFor();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, Instant.now().plus(Duration.ofMinutes(minutes)));
        return DummyProcess.INSTANCE;
    }

    @Override
    protected Process executeInternalWindows(Runtime runtime, String[] params) throws IOException {
        throw new RuntimeException("Not implemented");
    }
}
