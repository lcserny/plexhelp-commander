package net.cserny.search;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class LocalMediaSearchServiceTest {

    @Inject
    LocalMediaSearchService service;

    @Test
    @DisplayName("Check search works")
    public void checkSearch() {

    }
}
