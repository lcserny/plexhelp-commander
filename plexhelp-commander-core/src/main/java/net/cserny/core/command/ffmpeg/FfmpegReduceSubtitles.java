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

    private final LocalPathHandler localPathHandler;

    public FfmpegReduceSubtitles(ServerCommandProperties properties,
                                 TaskScheduler taskScheduler,
                                 CommandRunner commandRunner,
                                 LocalPathHandler localPathHandler) {
        super(properties, taskScheduler, commandRunner);
        this.localPathHandler = localPathHandler;
    }

    @Override
    public CommandName name() {
        return CommandName.REDUCE_SUBS;
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
        if (params.length != 2 && params.length != 3 ) {
            throw new IllegalArgumentException("Invalid params provided to command, it needs [0] the sourceMediaFilePath, [1] the targetMediaFilePath and [2:optional] comma-separated list of subtitle stream indexes to be kept");
        }

        LocalPath sourceMediaPath = localPathHandler.toLocalPath(params[0]);
        if (sourceMediaPath.attributes().isDirectory()) {
            throw new IllegalArgumentException("Provided source media file path is a directory not a regular file: " + sourceMediaPath.path());
        }
        log.info("Source media file path received: {}", sourceMediaPath.path());

        LocalPath targetMediaPath = localPathHandler.toLocalPath(params[1]);
        if (targetMediaPath.attributes().isDirectory()) {
            throw new IllegalArgumentException("Provided target media file path is a directory not a regular file: " + targetMediaPath.path());
        }
        log.info("Target media file path received: {}", targetMediaPath.path());

        String indexesString = params.length == 3 ? params[2] : "";
        List<Integer> subtitleStreamIndexes = Arrays.stream(indexesString.split(",")).map(String::trim).map(Integer::parseInt).toList();
        log.info("Subtitles indexes provided: {}", subtitleStreamIndexes);

        Path parent = sourceMediaPath.path().getParent();
        if (parent.getFileName().toString().startsWith("Season")) {
            parent = parent.getParent();
        }

        Path mediaWithoutRootPath = parent.getParent().relativize(sourceMediaPath.path());
        Path tempDir = Path.of(System.getProperty("user.home"));
        if (sourceMediaPath.path().getName(0).startsWith("mnt")) {
            tempDir = sourceMediaPath.path().subpath(0, 2);
        }
        Path tmpMediaPath = tempDir.resolve("tmp").resolve(mediaWithoutRootPath);
        log.info("Temporary media path to use: {}", tmpMediaPath);
        try {
            localPathHandler.createDirectories(localPathHandler.toLocalPath(tmpMediaPath.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return buildCommands(sourceMediaPath, targetMediaPath, subtitleStreamIndexes, tmpMediaPath);
    }

    private static @NonNull List<String> buildCommands(LocalPath sourceMediaPath, LocalPath targetMediaPath, List<Integer> subtitleStreamIndexes, Path tmpMediaPath) {
        List<String> commands = new ArrayList<>(List.of("ffmpeg", "-v", "error", "-i", escaped(sourceMediaPath.path().toString()), "-map", "0:v", "-map", "0:a"));

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
        commands.add(escaped(targetMediaPath.path().toString()));

        return commands;
    }
}
