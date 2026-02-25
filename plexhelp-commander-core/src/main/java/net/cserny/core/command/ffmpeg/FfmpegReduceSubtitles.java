package net.cserny.core.command.ffmpeg;

import lombok.extern.slf4j.Slf4j;
import net.cserny.api.DirectoryCreator;
import net.cserny.api.LocalPathConverter;
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
import java.util.List;

import static net.cserny.support.UtilityProvider.quoted;

@Slf4j
@Component
public class FfmpegReduceSubtitles extends AbstractOSCommand {

    public static final String NAME = "ffmpeg-reduce-subs";

    private final LocalPathConverter localPathConverter;
    private final DirectoryCreator directoryCreator;

    public FfmpegReduceSubtitles(ServerCommandProperties properties,
                                 TaskScheduler taskScheduler,
                                 CommandRunner commandRunner,
                                 LocalPathConverter localPathConverter,
                                 DirectoryCreator directoryCreator) {
        super(properties, taskScheduler, commandRunner);
        this.localPathConverter = localPathConverter;
        this.directoryCreator = directoryCreator;
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
    protected List<String> produceCommandLinux(String[] params) {
        if (params.length != 2) {
            throw new IllegalArgumentException("No params provided to command, it needs 2 params: inputMediaFilePath and nrSubtitlesToKeep");
        }

        LocalPath mediaPath = localPathConverter.toLocalPath(params[0]);
        if (mediaPath.attributes().isDirectory()) {
            throw new IllegalArgumentException("Provided media file path is a directory not a regular file: " + mediaPath.path());
        }
        log.info("Media file path received: {}", mediaPath.path());

        int nrSubtitles = Integer.parseInt(params[1]);
        log.info("Number of subtitles to keep: {}", nrSubtitles);

        Path parent = mediaPath.path().getParent();
        if (parent.getFileName().toString().startsWith("Season")) {
            parent = parent.getParent();
        }

        Path mediaWithoutRootPath = parent.getParent().relativize(mediaPath.path());
        Path systemTempDir = Path.of(System.getProperty("java.io.tmpdir"));
        Path tmpMediaPath = systemTempDir.resolve(mediaWithoutRootPath);
        log.info("Temporary media path to use: {}", tmpMediaPath);
        try {
            directoryCreator.createDirectories(localPathConverter.toLocalPath(tmpMediaPath.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return buildCommands(mediaPath, nrSubtitles, tmpMediaPath);
    }

    private static @NonNull List<String> buildCommands(LocalPath mediaPath, int nrSubtitles, Path tmpMediaPath) {
        List<String> commands = new ArrayList<>(List.of("ffmpeg", "-v", "error", "-i", quoted(mediaPath.path().toString()), "-map", "0:v", "-map", "0:a"));

        for (int i = 0; i < nrSubtitles; i++) {
            commands.add("-map");
            commands.add("0:s:" + i);
        }

        commands.add("-c");
        commands.add("copy");
        commands.add(quoted(tmpMediaPath.toString()));
        commands.add("-y");

        commands.add("&&");
        commands.add("mv");
        commands.add(quoted(tmpMediaPath.toString()));
        commands.add(quoted(mediaPath.path().toString()));

        return commands;
    }
}
