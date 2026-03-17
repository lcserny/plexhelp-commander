package net.cserny.task.move;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.AbstractCliCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Slf4j
@RequiredArgsConstructor
@Component
@CommandLine.Command(name = "adjust-date", description = "Updates movedMedia collection by renaming media files/folders date pattern to year pattern.")
public class AdjustMediaDateCommand extends AbstractCliCommand {

    private final AdjustMediaDateService adjustMediaDateService;

    @CommandLine.Option(names = {"-p", "--path"}, required = true, description = "The file path containing the backup of the rename.")
    private String path;

    @Override
    protected void run() throws Exception {
        adjustMediaDateService.adjustDate(path);
    }
}
