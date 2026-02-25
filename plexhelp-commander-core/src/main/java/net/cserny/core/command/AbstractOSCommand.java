package net.cserny.core.command;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.config.ServerCommandProperties;
import net.cserny.core.command.CommandRunner.CommandResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.scheduling.TaskScheduler;

@SuppressWarnings("LoggingSimilarMessage")
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractOSCommand implements Command {

    private final ServerCommandProperties properties;
    private final TaskScheduler taskScheduler;
    private final CommandRunner commandRunner;

    protected abstract List<String> produceCommandWindows(String[] params);

    protected abstract List<String> produceCommandLinux(String[] params);

    protected boolean executeNow() {
        return false;
    }

    protected boolean executeWslUsingWindows() {
        return true;
    }

    @Override
    public Optional<CommandResult> execute(String[] params) throws Exception {
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

    private Optional<CommandResult> executeInternal(List<String> commands) throws ExecutionException, InterruptedException {
        return executeInternal(commands, 0);
    }

    private Optional<CommandResult> executeInternal(List<String> commands, int minutes) throws ExecutionException, InterruptedException {
        CompletableFuture<CommandResult> future = new CompletableFuture<>();
        Runnable runnable = () -> {
            try {
                CommandResult result = commandRunner.run(commands);
                if (result.exitCode() != 0) {
                    throw new RuntimeException("Error executing command [code: %d] %s".formatted(result.exitCode(), result.response()));
                }
                log.info("CMD output: {}", result.response());
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        };

        if (executeNow() || minutes <= 0) {
            taskScheduler.schedule(runnable, Instant.now().plusMillis(50));
            return Optional.of(future.get());
        }

        taskScheduler.schedule(runnable, Instant.now().plus(Duration.ofMinutes(minutes)));
        return Optional.empty();
    }

    protected String getSystem32Prefix() {
        if (properties.getWsl().isEnabled()) {
            return properties.getWsl().getSystem32Path();
        }
        return "";
    }
}
