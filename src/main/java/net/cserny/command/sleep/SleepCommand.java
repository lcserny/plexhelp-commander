package net.cserny.command.sleep;

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
import java.util.Arrays;

@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
@Component
@RequiredArgsConstructor
public class SleepCommand extends AbstractOSCommand {

    private static final String NAME = "sleep";

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
            log.info("Sleeping system with params: {}", Arrays.toString(params));
            throw new UnsupportedOperationException("Sleeping with random params not supported");
        }
    }

    private String[] getCommandParts() {
        return new String[]{"systemctl", "suspend"};
    }

    private Process execWithoutDelay(Runtime runtime) throws IOException {
        log.info("Sleeping system without delay");
        return runtime.exec(getCommandParts());
    }

    private Process execWithDelay(Runtime runtime, int minutes) throws IOException {
        log.info("Sleeping system in {} minutes", minutes);
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                runtime.exec(getCommandParts());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, Duration.ofMinutes(minutes));
        return DummyProcess.INSTANCE;
    }

    @Override
    protected Process executeInternalWindows(Runtime runtime, String[] params) throws IOException {
        throw new RuntimeException("Not implemented");
    }
}
