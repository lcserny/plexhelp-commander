package net.cserny;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@SpringBootApplication
@EnableMongoRepositories
@EnableScheduling
public class CommanderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommanderApplication.class, args);
    }

    // TODO move to utility class

    public static String toOneLineString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.toString().replace("\n", " - ");
    }

    public static  <T> T getUnchecked(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
