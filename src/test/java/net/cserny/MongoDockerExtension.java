package net.cserny;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.List;

public class MongoDockerExtension implements BeforeAllCallback, AfterAllCallback {

    // port used in test properties file also
    static int MONGO_PORT = 37017;

    private GenericContainer<?> mongoDbContainer;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        mongoDbContainer = new GenericContainer<>("mongo:5.0")
                .withExposedPorts(MONGO_PORT)
                // credentails used in test properties file also
                .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
                .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example")
                .waitingFor(Wait.forLogMessage(".*Waiting for connections.*", 1));
        mongoDbContainer.setPortBindings(List.of(MONGO_PORT + ":27017"));
        mongoDbContainer.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        mongoDbContainer.stop();
    }
}
