package net.cserny;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.task.TaskCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;
import picocli.spring.PicocliSpringFactory;

@RequiredArgsConstructor
@Slf4j
@SpringBootApplication
public class TaskApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TaskApplication.class);
        ConfigurableApplicationContext context = app.run(args);

        TaskCommand taskCommand = context.getBean(TaskCommand.class);

        PicocliSpringFactory springFactory = new PicocliSpringFactory(context);
        CommandLine commandLine = new CommandLine(taskCommand, springFactory);

        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
