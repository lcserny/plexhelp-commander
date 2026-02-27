package net.cserny;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.LocalPathHandler;
import net.cserny.api.MediaIdentifier;
import net.cserny.config.FilesystemProperties;
import net.cserny.core.command.Command.CommandResult;
import net.cserny.core.command.LocalCommandService;
import net.cserny.core.command.ffmpeg.FfmpegReduceSubtitles;
import net.cserny.core.command.ffmpeg.FfmpegScanStreams;
import net.cserny.fs.LocalPath;
import net.cserny.support.Features;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO: impl spring shell with an enum for the operation to do and such

@RequiredArgsConstructor
@Slf4j
@SpringBootApplication
public class TestRunner implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TestRunner.class, args);
    }



    private final MediaIdentifier mediaIdentifier;
    private final LocalPathHandler localPathHandler;
    private final FilesystemProperties filesystemProperties;
    private final FeatureManager featureManager;
    private final LocalCommandService localCommandService;

    @Override
    public void run(String... args) throws Exception {
        // TODO needed, put someplace automatically used
        featureManager.setFeatureState(new FeatureState(Features.AUTOMOVE, false));




        log.info("Running TestRunner");

        LocalPath walkPath = localPathHandler.toLocalPath(filesystemProperties.getTvPath());
        List<LocalPath> filesFound = localPathHandler.walk(walkPath, 4);

        filesFound.forEach(localPath -> {
            if (!mediaIdentifier.isMedia(localPath)) {
                return;
            }

            Optional<CommandResult<List<Integer>>> scanResultOptional = localCommandService.execute(FfmpegScanStreams.NAME, new String[]{localPath.path().toString()});
            if (scanResultOptional.isEmpty()) {
                return;
            }

            CommandResult<List<Integer>> scanResult = scanResultOptional.get();
            if (!scanResult.success()) {
                throw new RuntimeException("Failed to scan for subtitles for " + localPath.path());
            }

            if (scanResult.result().isEmpty()) {
                return;
            }

            List<Integer> subtitleIndexes = scanResult.result();
            String subtitleIndexesString = subtitleIndexes.stream().map(String::valueOf).collect(Collectors.joining(","));
            Optional<CommandResult<String>> reduceResultOptional = localCommandService.execute(FfmpegReduceSubtitles.NAME, new String[]{localPath.path().toString(), subtitleIndexesString});
            if (reduceResultOptional.isEmpty()) {
                return;
            }

            CommandResult<String> reduceResult = reduceResultOptional.get();
            if (!reduceResult.success()) {
                throw new RuntimeException("Failed to reduce subtitles for " + localPath.path());
            }
        });
    }
}
