package net.cserny.core.rename;

import net.cserny.support.TVDataExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TVDataExtractorTest {

    @ParameterizedTest
    @MethodSource("seasonProvider")
    @DisplayName("finding season with regex correctly")
    void findSeason(String input, Integer expected) {
        assertThat(TVDataExtractor.findSeason(input)).isEqualTo(expected);
    }

    static Stream<Arguments> seasonProvider() {
        return Stream.of(
                Arguments.of("Cinderella.S04E07.1080p.tgx", 4),
                Arguments.of("Cinderella.Season.2 E07.1080p.tgx", 2),
                Arguments.of("Cinderella.Season-E07.1080p.tgx", null),
                Arguments.of("Cinderella season.3 E07.1080p.tgx", 3),
                Arguments.of("Cinderella season1 E07.1080p.tgx", 1),
                Arguments.of("Cinderella Season 08 E07.1080p.tgx", 8),
                Arguments.of("Over.the.Garden.Wall.Part1.The.Old.Grist.Mill.720p.HDTV.x264-W4F/over.the.garden.wall.part1.the.old.grist.mill.720p.hdtv.x264-w4f.mkv", null),
                Arguments.of("Cinderella 3rd season E07.1080p.tgx", 3)
        );
    }

    @Test
    @DisplayName("multiple seasons in name will get null")
    void findLastSeason() {
        assertThat(TVDataExtractor.findSeason("Atlanta (2016) Season 1-4 S01-S04 (1080p AMZN WEB-DL x265 HEVC 10bit EAC3 5.1 Silence)")).isNull();
    }

    @ParameterizedTest
    @MethodSource("episodeProvider")
    @DisplayName("finding episode with regex correctly")
    void findEpisode(String input, Integer expected) {
        assertThat(TVDataExtractor.findEpisode(input)).isEqualTo(expected);
    }

    static Stream<Arguments> episodeProvider() {
        return Stream.of(
                Arguments.of("Cinderella.S05E07.1080p.tgx", 7),
                Arguments.of("Cinderella.S05E9.1080p.tgx", 9),
                Arguments.of("Cinderella.S05e12.1080p.tgx", 12),
                Arguments.of("Cinderella.S05.1080p.tgx", null),
                Arguments.of("Cinderella.e1.1080p.tgx", 1),
                Arguments.of("Cinderella 2nd Season - 05 1080p.tgx", 5),
                Arguments.of("[DB]Kusuriya no Hitorigoto_-_03_(Dual Audio_10bit_BD1080p_x265).mkv", 3),
                Arguments.of("Over.the.Garden.Wall.Part1.The.Old.Grist.Mill.720p.HDTV.x264-W4F/over.the.garden.wall.part1.the.old.grist.mill.720p.hdtv.x264-w4f.mkv", 1),
                Arguments.of("[Anime Time] Solo Leveling - 001.mp4", 1),
                Arguments.of("[Anime Time] Solo Leveling - 1080.mp4", null)
        );
    }
}