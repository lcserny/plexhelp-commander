package net.cserny.task.clean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.AbstractCliCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Slf4j
@RequiredArgsConstructor
@Component
@CommandLine.Command(name = "clean-empty", description = "Removes folders without valid media files in them.")
public class CleanEmptyCommand extends AbstractCliCommand {

    private final CleanEmptyService  cleanEmptyService;

    @Override
    protected void run() throws Exception {
        cleanEmptyService.run();
    }
}
