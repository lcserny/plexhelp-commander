package net.cserny;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.core.command.CommandRunner;
import net.cserny.core.command.CommandRunner.CommandResult;
import net.cserny.core.command.LocalCommandService;
import net.cserny.core.command.ffmpeg.FfmpegScanStreams;
import net.cserny.generated.CommandResponse;
import net.cserny.support.Features;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;

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
        Optional<CommandResult> result = localCommandService.execute(FfmpegScanStreams.NAME, new String[]{"/mnt/e/Videos/2021-11-03 15-01-32 - Interviu adoptie Alina.flv"});
        log.info("{}", result.get());
    }
}
