package net.cserny.rename;

import org.apache.commons.text.similarity.LevenshteinDistance;

// TODO add tests
public class SimilarityService {

    private static final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

    private SimilarityService() {
    }

    public static int getDistance(String source, String compare) {
        return levenshteinDistance.apply(source, compare);
    }

    public static int getSimilarityPercent(int distance, int targetSize) {
        return (int)((float)(targetSize - distance) / (float)targetSize * 100);
    }
}
