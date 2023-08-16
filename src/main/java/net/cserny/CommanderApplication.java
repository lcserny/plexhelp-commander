package net.cserny;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class CommanderApplication {

    @Autowired
    ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(CommanderApplication.class, args);
    }
}
