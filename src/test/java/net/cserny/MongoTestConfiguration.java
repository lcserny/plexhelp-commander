package net.cserny;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

@TestConfiguration(proxyBeanMethods = false)
@EnableMongoRepositories
public class MongoTestConfiguration {

    private static final String MONGO_URI_KEY = "spring.data.mongodb.uri";

    @Container
    public static MongoDBContainer container = new MongoDBContainer("mongo:7.0");

    static {
        container.start();
        System.setProperty(MONGO_URI_KEY, container.getConnectionString());
    }

    @Bean
    public MongoDBContainer mongoDbContainer() {
        return container;
    }

    @DynamicPropertySource
    static void setMongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add(MONGO_URI_KEY, container::getReplicaSetUrl);
    }
}
