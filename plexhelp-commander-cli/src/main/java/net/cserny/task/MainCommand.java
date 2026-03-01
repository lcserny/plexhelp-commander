package net.cserny.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.task.clean.CleanEmptyCommand;
import net.cserny.task.subs.ReduceSubtitlesCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Slf4j
@Component
@Command(name = "main",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Runs a Commander task based on the provided argument.",
        subcommands = {
                ReduceSubtitlesCommand.class,
                CleanEmptyCommand.class
        }
)
@RequiredArgsConstructor
public class MainCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        log.warn("Use 'main --help' for options.");
        return 1;
    }
}
