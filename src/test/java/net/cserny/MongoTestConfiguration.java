package net.cserny;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

@Configuration
@EnableMongoRepositories
public class MongoTestConfiguration {

    @Container
    public static MongoDBContainer container = new MongoDBContainer("mongo:5.0");

    static {
        container.start();
        System.setProperty("spring.data.mongodb.uri", container.getConnectionString());
    }
}
