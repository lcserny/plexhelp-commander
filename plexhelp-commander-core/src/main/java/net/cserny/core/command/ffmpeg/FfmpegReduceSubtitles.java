package net.cserny.core.command.ffmpeg;

import lombok.extern.slf4j.Slf4j;
import net.cserny.api.LocalPathHandler;
import net.cserny.config.ServerCommandProperties;
import net.cserny.core.command.AbstractOSCommand;
import net.cserny.core.command.CommandRunner;
import net.cserny.fs.LocalPath;
import org.jspecify.annotations.NonNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.cserny.support.UtilityProvider.escaped;

@Slf4j
@Component
public class FfmpegReduceSubtitles extends AbstractOSCommand<String> {

    public static final String NAME = "ffmpeg-reduce-subs";

    private final LocalPathHandler localPathHandler;

    public FfmpegReduceSubtitles(ServerCommandProperties properties,
                                 TaskScheduler taskScheduler,
                                 CommandRunner commandRunner,
                                 LocalPathHandler localPathHandler) {
        super(properties, taskScheduler, commandRunner);
        this.localPathHandler = localPathHandler;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected boolean executeNow() {
        return true;
    }

    @Override
    protected boolean executeWslUsingWindows() {
        return false;
    }

    @Override
    protected List<String> produceCommandWindows(String[] params) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String adaptOutput(String output) {
        return output;
    }

    @Override
    protected List<String> produceCommandLinux(String[] params) {
        if (params.length != 1 && params.length != 2 ) {
            throw new IllegalArgumentException("No (or invalid) params provided to command, it needs at least the inputMediaFilePath and a (optional) comma-separated list of subtitle stream indexes");
        }

        LocalPath mediaPath = localPathHandler.toLocalPath(params[0]);
        if (mediaPath.attributes().isDirectory()) {
            throw new IllegalArgumentException("Provided media file path is a directory not a regular file: " + mediaPath.path());
        }
        log.info("Media file path received: {}", mediaPath.path());

        String indexesString = params.length == 2 ? params[1] : "";
        List<Integer> subtitleStreamIndexes = Arrays.stream(indexesString.split(",")).map(String::trim).map(Integer::parseInt).toList();
        log.info("Subtitles indexes provided: {}", subtitleStreamIndexes);

        Path parent = mediaPath.path().getParent();
        if (parent.getFileName().toString().startsWith("Season")) {
            parent = parent.getParent();
        }

        Path mediaWithoutRootPath = parent.getParent().relativize(mediaPath.path());
        Path tempDir = Path.of(System.getProperty("user.home"));
        if (mediaPath.path().getName(0).startsWith("mnt")) {
            tempDir = mediaPath.path().subpath(0, 2);
        }
        Path tmpMediaPath = tempDir.resolve("tmp").resolve(mediaWithoutRootPath);
        log.info("Temporary media path to use: {}", tmpMediaPath);
        try {
            localPathHandler.createDirectories(localPathHandler.toLocalPath(tmpMediaPath.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return buildCommands(mediaPath, subtitleStreamIndexes, tmpMediaPath);
    }

    private static @NonNull List<String> buildCommands(LocalPath mediaPath, List<Integer> subtitleStreamIndexes, Path tmpMediaPath) {
        List<String> commands = new ArrayList<>(List.of("ffmpeg", "-v", "error", "-i", escaped(mediaPath.path().toString()), "-map", "0:v", "-map", "0:a"));

        for (Integer i : subtitleStreamIndexes) {
            commands.add("-map");
            commands.add("0:" + i);
        }

        commands.add("-c");
        commands.add("copy");
        commands.add(escaped(tmpMediaPath.toString()));
        commands.add("-y");

        commands.add("&&");
        commands.add("mv");
        commands.add(escaped(tmpMediaPath.toString()));
        commands.add(escaped(mediaPath.path().toString()));

        return commands;
    }
}
