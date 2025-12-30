package net.cserny.command;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.command.OsExecutor.ExecutionResponse;
import net.cserny.generated.CommandResponse;
import net.cserny.generated.Status;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.scheduling.TaskScheduler;

@SuppressWarnings("LoggingSimilarMessage")
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractOSCommand implements Command {

    private final ServerCommandProperties properties;
    private final TaskScheduler taskScheduler;
    private final OsExecutor osExecutor;

    protected abstract List<String> produceCommandWindows(String[] params);

    protected abstract List<String> produceCommandLinux(String[] params);

    @Override
    public CommandResponse execute(String[] params) {
        List<String> commands;
        if (SystemUtils.IS_OS_WINDOWS || properties.getWsl().isEnabled()) {
            log.info("Producing commands for Windows (or WSL)");
            commands = produceCommandWindows(params);
        } else if (SystemUtils.IS_OS_LINUX) {
            log.info("Producing commands for Linux");
            commands = produceCommandLinux(params);
        } else {
            throw new RuntimeException("Unsupported operating system: " + SystemUtils.OS_NAME);
        }

        try {
            if (params.length == 0) {
                log.info("Executing commands without delay");
                executeInternal(commands);
            } else {
                if (StringUtils.isNumeric(params[0])) {
                    int minutes = Integer.parseInt(params[0]);
                    if (minutes > 0) {
                        log.info("Executing commands in {} minutes", minutes);
                        executeInternal(commands, minutes);
                    } else {
                        log.info("Executing commands without delay");
                        executeInternal(commands);
                    }
                } else {
                    log.info("Executing commands without delay");
                    executeInternal(commands);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new CommandResponse(Status.FAILED);
        }

        return Command.EMPTY_OK;
    }

    private void executeInternal(List<String> commands) throws Exception {
        ExecutionResponse response = osExecutor.execute(commands);
        if (response.exitCode() != 0) {
            throw new RuntimeException(response.response());
        }
        log.info("CMD output: {}", response.response());
    }

    private void executeInternal(List<String> commands, int minutes) throws Exception {
        taskScheduler.schedule(() -> {
                try {
                    executeInternal(commands);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },
            Instant.now().plus(Duration.ofMinutes(minutes))
        );
    }

    protected String getCommandPrefix() {
        if (properties.getWsl().isEnabled()) {
            return properties.getWsl().getSystem32Path();
        }
        return "";
    }
}
