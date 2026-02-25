package net.cserny.core.command.ffmpeg;

import lombok.extern.slf4j.Slf4j;
import net.cserny.config.ServerCommandProperties;
import net.cserny.core.command.AbstractOSCommand;
import net.cserny.core.command.CommandRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.cserny.support.UtilityProvider.quoted;

@Slf4j
@Component
public class FfmpegScanStreams extends AbstractOSCommand {

    public static final String NAME = "ffmpeg-scan-subs";

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

        String mediaPath = params[0];
        log.info("Media file path received: {}", mediaPath);

        // complicated command but necessary for proper exit codes by either grep (if nothing found) or other cmds when they fail
        return List.of("(set", "-o", "pipefail;", "ffprobe", "-v", "error", "-show_streams", "-select_streams", "s", quoted(mediaPath), "|", "(grep", "-c", "codec_type=s", "||", "true))");
    }
}
