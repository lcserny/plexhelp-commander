package net.cserny;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.cserny.support.Features;
import net.cserny.task.MainCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import picocli.CommandLine;

@RequiredArgsConstructor
@SpringBootApplication
public class TaskApplication implements CommandLineRunner {

    private final FeatureManager featureManager;
    private final CommandLine.IFactory iFactory;
    private final MainCommand mainCommand;

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(TaskApplication.class, args)));
    }

    @PostConstruct
    public void disableServerFeatures() {
        featureManager.setFeatureState(new FeatureState(Features.AUTOMOVE, false));
    }

    @Override
    public void run(String... args) {
        int exitCode = new CommandLine(mainCommand, iFactory).execute(args);
        System.exit(exitCode);
    }
}
