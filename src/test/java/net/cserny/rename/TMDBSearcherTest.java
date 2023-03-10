package net.cserny.rename;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.MongoTestSetup;
import net.cserny.rename.NameNormalizer.NameYear;
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
        NameYear movie1 = new NameYear("fight club", 2000);
        RenamedMediaOptions options1 = searcher.search(movie1, MediaFileType.MOVIE);
        System.out.println(options1);

        NameYear movie2 = new NameYear("marley & me", 2008);
        RenamedMediaOptions options2 = searcher.search(movie2, MediaFileType.MOVIE);
        System.out.println(options2);

        NameYear tv1 = new NameYear("game of thrones", null);
        RenamedMediaOptions options3 = searcher.search(tv1, MediaFileType.TV);
        System.out.println(options3);
    }
}