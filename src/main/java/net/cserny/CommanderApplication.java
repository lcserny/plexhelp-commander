package net.cserny;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMongoRepositories
@EnableScheduling
public class CommanderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommanderApplication.class, args);
    }

    public static String toOneLineString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.toString().replace("\n", " - ");
    }
}
