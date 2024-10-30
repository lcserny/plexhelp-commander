package net.cserny.rename;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TVSeriesHelperTest {

    @Test
    @DisplayName("finding season with regex correctly")
    public void findSeason() {
        assertEquals(4, TVSeriesHelper.findSeason("Cinderella.S04E07.1080p.tgx"));
        assertEquals(2, TVSeriesHelper.findSeason("Cinderella.Season.2 E07.1080p.tgx"));
        assertNull(TVSeriesHelper.findSeason("Cinderella.Season-E07.1080p.tgx"));
        assertEquals(3, TVSeriesHelper.findSeason("Cinderella season.3 E07.1080p.tgx"));
    }

    @Test
    @DisplayName("finding episode with regex correctly")
    public void findEpisode() {
        assertEquals(7, TVSeriesHelper.findEpisode("Cinderella.S05E07.1080p.tgx"));
        assertEquals(9, TVSeriesHelper.findEpisode("Cinderella.S05E9.1080p.tgx"));
        assertEquals(12, TVSeriesHelper.findEpisode("Cinderella.S05e12.1080p.tgx"));
        assertNull(TVSeriesHelper.findEpisode("Cinderella.S05.1080p.tgx"));
        assertEquals(1, TVSeriesHelper.findEpisode("Cinderella.e1.1080p.tgx"));
    }
}