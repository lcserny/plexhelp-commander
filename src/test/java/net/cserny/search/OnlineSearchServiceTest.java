package net.cserny.search;

import io.quarkus.test.junit.QuarkusTest;
import net.cserny.MongoDockerExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;

@QuarkusTest
@Testcontainers
public class OnlineSearchServiceTest {

    @RegisterExtension
    static final MongoDockerExtension deploy = new MongoDockerExtension();

    @Inject
    OnlineSearchService service;

    @Test
    @DisplayName("Check that the mongo container starts")
    void containerStarts() {
    }
}
