package net.cserny;

import lombok.extern.slf4j.Slf4j;
import net.cserny.command.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

// TODO, remove unneeded stuff
@Slf4j
@SpringBootApplication
@EnableMongoRepositories
@EnableScheduling
public class CommanderApplication implements CommandLineRunner {

    @Autowired
    ApplicationContext context;

    @Autowired
    List<Command> commands;

    public static void main(String[] args) {
        SpringApplication.run(CommanderApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Commands:");
        for (Command command : commands) {
            log.info(command.toString());
        }
    }
}
