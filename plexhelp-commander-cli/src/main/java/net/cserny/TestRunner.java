package net.cserny;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.core.command.Command.CommandResult;
import net.cserny.core.command.LocalCommandService;
import net.cserny.core.command.ffmpeg.FfmpegReduceSubtitles;
import net.cserny.core.command.ffmpeg.FfmpegScanStreams;
import net.cserny.support.Features;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;

import java.util.List;
import java.util.Optional;

// TODO: impl spring shell with an enum for the operation to do and such

@RequiredArgsConstructor
@Slf4j
@SpringBootApplication
public class TestRunner implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TestRunner.class, args);
    }


    private final FeatureManager featureManager;
    private final LocalCommandService localCommandService;

    @Override
    public void run(String... args) throws Exception {
        // TODO needed, put someplace automatically used
        featureManager.setFeatureState(new FeatureState(Features.AUTOMOVE, false));




        log.info("Running TestRunner");
//        Optional<CommandResult<List<Integer>>> scanResult = localCommandService.execute(FfmpegScanStreams.NAME, new String[]{"/mnt/e/Videos/2021-11-03 15-01-32 - Interviu adoptie Alina.flv"});
//        Optional<CommandResult<List<Integer>>> scanResult = localCommandService.execute(FfmpegScanStreams.NAME, new String[]{"/mnt/e/Videos/Movies/A Complete Unknown (2024-12-18)/A Complete Unknown (2024-12-18).mp4"});
//        Optional<CommandResult<List<Integer>>> scanResult = localCommandService.execute(FfmpegScanStreams.NAME, new String[]{"/mnt/e/Videos/TV/Wednesday (2022-11-23)/Season 2/Wednesday S02E01 (2022-11-23).mkv"});
        Optional<CommandResult<List<Integer>>> scanResult = localCommandService.execute(FfmpegScanStreams.NAME, new String[]{"/mnt/e/Videos/Yellowstone S05E01 (2018-06-20).mkv"});
        log.info("{}", scanResult.get());

        Optional<CommandResult<String>> convertResult = localCommandService.execute(FfmpegReduceSubtitles.NAME, new String[]{"/mnt/e/Videos/Yellowstone S05E01 (2018-06-20).mkv", "2,3"});
        log.info("{}", convertResult.get());
    }
}
