package net.cserny.search;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.MongoTestSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;

@QuarkusTest
@Testcontainers
@QuarkusTestResource(MongoTestSetup.class)
public class OnlineSearchServiceTest {

    @Inject
    OnlineSearchService service;

    @Test
    @DisplayName("Check that the mongo container starts")
    void containerStarts() {
    }
}
