package net.cserny.cache;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;

@QuarkusTest
@Testcontainers
public class DownloadedMediaServiceTest {

    @RegisterExtension
    static final MongoDockerExtension deploy = new MongoDockerExtension();

    @Inject
    DownloadedMediaService service;

    @Test
    @DisplayName("Check that the mongo container starts")
    void containerStarts() {
    }
}
