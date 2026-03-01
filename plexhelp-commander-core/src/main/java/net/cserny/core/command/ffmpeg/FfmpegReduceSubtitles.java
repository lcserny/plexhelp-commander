package net.cserny.core.command.ffmpeg;

import lombok.extern.slf4j.Slf4j;
import net.cserny.api.LocalPathHandler;
import net.cserny.config.ServerCommandProperties;
import net.cserny.core.command.AbstractOSCommand;
import net.cserny.core.command.CommandRunner;
import net.cserny.api.dto.LocalPath;
import org.jspecify.annotations.NonNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.cserny.support.UtilityProvider.escaped;

@Slf4j
@Component
public class FfmpegReduceSubtitles extends AbstractOSCommand<String> {

    private static final String suffix = "_tmp";

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

        LocalPath sourceMediaPath = getMediaLocalPath(params[0]);
        log.info("Source media file path received: {}", sourceMediaPath.path());

        LocalPath targetMediaPath =  getMediaLocalPath(params[1]);
        log.info("Target media file path received: {}", targetMediaPath.path());

        String indexesString = params.length == 3 ? params[2] : "";
        List<Integer> subtitleStreamIndexes = Arrays.stream(indexesString.split(",")).map(String::trim).map(Integer::parseInt).toList();
        log.info("Subtitles indexes provided: {}", subtitleStreamIndexes);

        return buildCommands(sourceMediaPath.toString(), targetMediaPath.toString(), subtitleStreamIndexes);
    }

    private @NonNull LocalPath getMediaLocalPath(String path) {
        LocalPath localPath = localPathHandler.toLocalPath(path);
        if (localPath.attributes().isDirectory()) {
            throw new IllegalArgumentException("Provided media file path is a directory not a regular file: " + path);
        }
        return localPath;
    }

    private static @NonNull List<String> buildCommands(String sourceMediaPath, String targetMediaPath, List<Integer> subtitleStreamIndexes) {
        List<String> commands = new ArrayList<>(List.of("ffmpeg", "-v", "error", "-i", escaped(sourceMediaPath), "-map", "0:v", "-map", "0:a"));

        for (Integer i : subtitleStreamIndexes) {
            commands.add("-map");
            commands.add("0:" + i);
        }

        String extractMediaPath = targetMediaPath;
        if (sourceMediaPath.equals(targetMediaPath)) {
            extractMediaPath = targetMediaPath + suffix;
        }

        commands.add("-c");
        commands.add("copy");
        commands.add(escaped(extractMediaPath));
        commands.add("-y");

        if (sourceMediaPath.equals(targetMediaPath)) {
            commands.add("&&");
            commands.add("mv");
            commands.add(escaped(extractMediaPath));
            commands.add(escaped(targetMediaPath));
        }

        commands.add("&&");
        commands.add("rm");
        commands.add(escaped(sourceMediaPath));

        return commands;
    }
}
