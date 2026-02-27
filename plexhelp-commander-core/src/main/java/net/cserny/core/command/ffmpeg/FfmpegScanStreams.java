package net.cserny.core.command.ffmpeg;

import lombok.extern.slf4j.Slf4j;
import net.cserny.config.ServerCommandProperties;
import net.cserny.core.command.AbstractOSCommand;
import net.cserny.core.command.CommandRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.cserny.support.UtilityProvider.quoted;

@Slf4j
@Component
public class FfmpegScanStreams extends AbstractOSCommand<List<Integer>> {

    public static final String NAME = "ffmpeg-scan-subs";

    private static final int maxItems = 5;

    private static final Pattern pattern = Pattern.compile("\\[STREAM](.*?)\\[/STREAM]", Pattern.DOTALL);
    private static final Pattern indexPattern = Pattern.compile("index=(\\d+)");

    private static final String matchSubstring = "codec_type=s";
    private static final String englishSubstring = "TAG:language=en";
    private static final String unknownSubstring = "TAG:language=";

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

        return List.of("ffprobe", "-v", "error", "-show_streams", "-select_streams", "s", quoted(mediaPath));
    }

    @Override
    protected List<Integer> adaptOutput(String output) {
        List<Integer> indexes = new ArrayList<>();

        Matcher matcher = pattern.matcher(output);
        while (matcher.find()) {
            String extracted = matcher.group(1);
            if (extracted.contains(matchSubstring)) {
                if (!extracted.contains(unknownSubstring) || extracted.contains(englishSubstring)) {
                    Matcher indexMatcher = indexPattern.matcher(extracted);
                    if (indexMatcher.find()) {
                        indexes.add(Integer.parseInt(indexMatcher.group(1)));
                    } else {
                        log.warn("Index of sub stream could not be found in: {}", extracted);
                    }
                }
            }
        }

        return indexes.subList(0, Math.min(maxItems, indexes.size()));
    }
}
