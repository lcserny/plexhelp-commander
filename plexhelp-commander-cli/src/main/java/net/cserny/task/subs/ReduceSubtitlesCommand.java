package net.cserny.task.subs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.concurrent.Callable;

@Slf4j
@RequiredArgsConstructor
@Component
@Command(name = "reduce-subs", description = "Reduces number of subtitle streams to improve DLNA compatibility.")
public class ReduceSubtitlesCommand implements Callable<Integer> {

    private final ReduceSubtitlesService reduceSubtitlesService;

    @Option(names = {"-p", "--path"}, required = true, description = "The path to the media files to scan and reduce in-place.")
    private String path;

    @Override
    public Integer call() {
        try {
            reduceSubtitlesService.run(path);
            return 0;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return 1;
        }
    }
}
