package net.cserny.rename;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TVDataExtractorTest {

    @Test
    @DisplayName("finding season with regex correctly")
    public void findSeason() {
        assertEquals(4, TVDataExtractor.findSeason("Cinderella.S04E07.1080p.tgx"));
        assertEquals(2, TVDataExtractor.findSeason("Cinderella.Season.2 E07.1080p.tgx"));
        assertNull(TVDataExtractor.findSeason("Cinderella.Season-E07.1080p.tgx"));
        assertEquals(3, TVDataExtractor.findSeason("Cinderella season.3 E07.1080p.tgx"));
        assertEquals(1, TVDataExtractor.findSeason("Cinderella season1 E07.1080p.tgx"));
        assertEquals(8, TVDataExtractor.findSeason("Cinderella Season 08 E07.1080p.tgx"));
        assertEquals(3, TVDataExtractor.findSeason("Cinderella 3rd season E07.1080p.tgx"));
    }

    @Test
    @DisplayName("multiple seasons in name wil get null")
    public void findLastSeason() {
        assertNull(TVDataExtractor.findSeason("Atlanta (2016) Season 1-4 S01-S04 (1080p AMZN WEB-DL x265 HEVC 10bit EAC3 5.1 Silence)"));
    }

    @Test
    @DisplayName("finding episode with regex correctly")
    public void findEpisode() {
        assertEquals(7, TVDataExtractor.findEpisode("Cinderella.S05E07.1080p.tgx"));
        assertEquals(9, TVDataExtractor.findEpisode("Cinderella.S05E9.1080p.tgx"));
        assertEquals(12, TVDataExtractor.findEpisode("Cinderella.S05e12.1080p.tgx"));
        assertNull(TVDataExtractor.findEpisode("Cinderella.S05.1080p.tgx"));
        assertEquals(1, TVDataExtractor.findEpisode("Cinderella.e1.1080p.tgx"));
        assertEquals(5, TVDataExtractor.findEpisode("Cinderella 2nd Season - 05 1080p.tgx"));
    }
}