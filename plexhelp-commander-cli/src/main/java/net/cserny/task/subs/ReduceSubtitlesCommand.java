package net.cserny.task.subs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.AbstractCliCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@RequiredArgsConstructor
@Component
@Command(name = "reduce-subs", description = "Reduces number of subtitle streams to improve DLNA compatibility.")
public class ReduceSubtitlesCommand extends AbstractCliCommand {

    private final ReduceSubtitlesService reduceSubtitlesService;

    @Option(names = {"-p", "--path"}, required = true, description = "The path to the media files to scan and reduce in-place.")
    private String path;

    @Override
    protected void run() throws Exception {
        reduceSubtitlesService.run(path);
    }
}
