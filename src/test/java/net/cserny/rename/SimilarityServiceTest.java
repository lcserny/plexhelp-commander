package net.cserny.rename;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimilarityServiceTest {

    @Test
    @DisplayName("getting distance between two text chunks")
    void getDistance() {
        var text1 = "Something";
        var text2 = "Somethin";
        assertEquals(1, SimilarityService.getDistance(text1, text2));
    }

    @Test
    @DisplayName("getting similarity percent between two text chunks")
    void getSimilarityPercent() {
        var text1 = "Something";
        var text2 = "Somethin";
        var distance = SimilarityService.getDistance(text1, text2);
        var target = text1.length();

        assertEquals(88, SimilarityService.getSimilarityPercent(distance, target));
    }
}