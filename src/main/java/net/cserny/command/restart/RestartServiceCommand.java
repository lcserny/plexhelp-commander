package net.cserny.command.restart;

import lombok.RequiredArgsConstructor;
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
public class RestartServiceCommand extends AbstractOSCommand {

    private static final String NAME = "restart-service";

    private final TaskScheduler scheduler;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected Process executeInternalLinux(Runtime runtime, String[] params) throws IOException {
        if (params.length == 1) {
            return execWithoutDelay(runtime, params[0]);
        } else if (params.length == 2 && StringUtils.isNumeric(params[1])) {
            int minutes = Integer.parseInt(params[1]);
            if (minutes == 0) {
                return execWithoutDelay(runtime, params[0]);
            } else {
                return execWithDelay(runtime, params[0], minutes);
            }
        }

        throw new UnsupportedOperationException("Restarting Service with random params not supported " + Arrays.toString(params));
    }

    private String[] getCommandParts(String serviceName) {
        return new String[]{"systemctl", "--user", "restart", serviceName};
    }

    private Process execWithoutDelay(Runtime runtime, String serviceName) throws IOException {
        log.info("Restarting {} Service without delay", serviceName);
        return runtime.exec(getCommandParts(serviceName));
    }

    private Process execWithDelay(Runtime runtime, String serviceName, int minutes) throws IOException {
        log.info("Restarting {} Service in {} minutes", serviceName, minutes);
        scheduler.schedule(() -> {
            try {
                runtime.exec(getCommandParts(serviceName)).waitFor();
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
