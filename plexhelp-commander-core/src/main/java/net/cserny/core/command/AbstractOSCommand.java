package net.cserny.core.command;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.Command;
import net.cserny.api.dto.CommandResult;
import net.cserny.config.ServerCommandProperties;
import net.cserny.core.command.CommandRunner.CommandOutput;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.scheduling.TaskScheduler;

@SuppressWarnings("LoggingSimilarMessage")
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractOSCommand<T> implements Command<T> {

    private final ServerCommandProperties properties;
    private final TaskScheduler taskScheduler;
    private final CommandRunner commandRunner;

    protected abstract List<String> produceCommandWindows(String[] params);

    protected abstract List<String> produceCommandLinux(String[] params);

    protected abstract T adaptImmediateOutput(String output);

    protected boolean executeNow() {
        return false;
    }

    protected boolean executeWslUsingWindows() {
        return true;
    }

    @Override
    public Optional<CommandResult<T>> execute(String[] params) throws Exception {
        List<String> commands;
        if (SystemUtils.IS_OS_WINDOWS || (properties.getWsl().isEnabled() && executeWslUsingWindows())) {
            log.info("Producing commands for Windows (or WSL)");
            commands = produceCommandWindows(params);
        } else if (SystemUtils.IS_OS_LINUX) {
            log.info("Producing commands for Linux");
            commands = produceCommandLinux(params);
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + SystemUtils.OS_NAME);
        }

        validateCommands(commands);

        // FIXME this is buggy, what if I have a command with a numeric first param which isn't a delay in mins?
        //  - need to add specific "delay" field in CommandRequest and send it from front
        if (params.length == 1 && StringUtils.isNumeric(params[0])) {
            int minutes = Integer.parseInt(params[0]);
            if (minutes > 0) {
                log.info("Executing commands in {} minutes", minutes);
                return executeInternal(commands, minutes);
            }
        }

        log.info("Executing commands without delay");
        return executeInternal(commands);
    }

    private void validateCommands(List<String> commands) {
        for (String part : commands) {
            if (part.contains(" ")) {
                if (!(part.startsWith("'") && part.endsWith("'"))) {
                    throw new IllegalArgumentException("Invalid command part, it contains a space but is not quoted (escaped): " + part);
                }
            }
        }
    }

    private Optional<CommandResult<T>> executeInternal(List<String> commands)
            throws ExecutionException, InterruptedException, TimeoutException {
        return executeInternal(commands, 0);
    }

    private Optional<CommandResult<T>> executeInternal(List<String> commands, int minutes)
            throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<CommandOutput> future = new CompletableFuture<>();
        Runnable runnable = () -> {
            try {
                CommandOutput output = commandRunner.run(commands);
                String loggableOutputResponse = StringUtils.truncate(output.response().replace("\n", " "), 100);
                if (output.exitCode() != 0) {
                    throw new RuntimeException("Error executing command [code: %d] %s".formatted(output.exitCode(), loggableOutputResponse));
                }
                log.info("CMD output: {}", loggableOutputResponse);
                future.complete(output);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        };

        if (!executeNow() && minutes > 0) {
            taskScheduler.schedule(runnable, Instant.now().plus(Duration.ofMinutes(minutes)));
            return Optional.of(new CommandResult<T>(true, true, null));
        }

        taskScheduler.schedule(runnable, Instant.now());
        String output = future.get(30, TimeUnit.SECONDS).response();
        T adaptedOutput = adaptImmediateOutput(output);
        return Optional.of(new CommandResult<>(true, false, adaptedOutput));
    }

    protected String getSystem32Prefix() {
        if (properties.getWsl().isEnabled()) {
            return properties.getWsl().getSystem32Path();
        }
        return "";
    }
}
