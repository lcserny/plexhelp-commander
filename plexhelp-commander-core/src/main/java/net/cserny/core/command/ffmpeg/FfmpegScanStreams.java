package net.cserny.core.command.ffmpeg;

import net.cserny.config.ServerCommandProperties;
import net.cserny.core.command.AbstractOSCommand;
import net.cserny.core.command.CommandRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FfmpegScanStreams extends AbstractOSCommand {

    public static final String NAME = "ffmpeg-scan";

    public FfmpegScanStreams(ServerCommandProperties properties,
                             TaskScheduler taskScheduler,
                             CommandRunner commandRunner) {
        super(properties, taskScheduler, commandRunner);
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
        if (params.length != 1) {
            throw new IllegalArgumentException("No param provided to command, it needs the media file input path");
        }
        return List.of("ffmpeg", "-i", "\"" + params[0] + "\"", "2>&1", "|", "grep", "-c", "\"Stream #\"");
    }
}
