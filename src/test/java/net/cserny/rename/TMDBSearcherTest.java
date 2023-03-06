package net.cserny.rename;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.MongoTestSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Testcontainers
@QuarkusTestResource(MongoTestSetup.class)
class TMDBSearcherTest {

    @Inject
    TMDBSearcher searcher;

    // TODO: mock TMDB calls

    @Test
    @DisplayName("Check that the mongo container starts")
    void containerStarts() {
    }
}