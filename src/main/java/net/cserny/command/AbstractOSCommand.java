package net.cserny.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.CommandResponse;
import net.cserny.generated.Status;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@SuppressWarnings("LoggingSimilarMessage")
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractOSCommand implements Command {

    private static final String COMMAND_PREFIX = "/mnt/c/Windows/System32/";

    private final ServerCommandProperties properties;
    private final TaskExecutor taskExecutor;
    private final TaskScheduler taskScheduler;

    protected abstract List<String> produceCommandWindows(String[] params);
    protected abstract List<String> produceCommandLinux(String[] params);

    @Override
    public CommandResponse execute(String[] params) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.redirectErrorStream(true);

        List<String> commands;
        if (SystemUtils.IS_OS_WINDOWS || properties.isWsl()) {
            log.info("Producing commands for Windows (or WSL)");
            commands = produceCommandWindows(params);
        } else if (SystemUtils.IS_OS_LINUX) {
            log.info("Producing commands for Linux");
            commands = produceCommandLinux(params);
        } else {
            throw new RuntimeException("Unsupported operating system: " + SystemUtils.OS_NAME);
        }
        builder.command(commands);

        try {
            if (params.length == 0) {
                log.info("Executing commands without delay");
                executeInternal(builder);
            } else {
                if (StringUtils.isNumeric(params[0])) {
                    int minutes = Integer.parseInt(params[0]);
                    if (minutes > 0) {
                        log.info("Executing commands in {} minutes", minutes);
                        executeInternal(builder, minutes);
                    } else {
                        log.info("Executing commands without delay");
                        executeInternal(builder);
                    }
                } else {
                    log.info("Executing commands without delay");
                    executeInternal(builder);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new CommandResponse(Status.FAILED);
        }

        return Command.EMPTY;
    }

    private void executeInternal(ProcessBuilder builder) throws Exception {
        Process process = builder.start();
        captureOutput(process);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.warn("Process executed and exited with non-zero exit code: {}", exitCode);
        }
    }

    private void executeInternal(ProcessBuilder builder, int minutes) throws Exception {
        taskScheduler.schedule(() -> {
            try {
                executeInternal(builder);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, Instant.now().plus(Duration.ofMinutes(minutes)));
    }

    private void captureOutput(Process process) {
        taskExecutor.execute(() -> {
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[OS_PROCESS_OUTPUT]: {}", line);
                }
            } catch (IOException e) {
                log.error("Error reading process stream", e);
            }
        });
    }

    protected String getCommandPrefix() {
        if (properties.isWsl()) {
            return COMMAND_PREFIX;
        }
        return "";
    }
}
