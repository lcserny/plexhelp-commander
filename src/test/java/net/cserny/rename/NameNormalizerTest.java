package net.cserny.rename;

import net.cserny.IntegrationTest;
import net.cserny.rename.NameNormalizer.NameYear;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NameNormalizerTest extends IntegrationTest {

    @Autowired
    NameNormalizer normalizer;

    private void checkNormalizedFormatted(String name, String expected) {
        NameYear nameYear = normalizer.normalize(name);
        assertEquals(expected, nameYear.formatted());
    }

    @Test
    @DisplayName("Check that prenormalized is detected correctly")
    void checkPrenormalizedOriginName() {
        checkNormalizedFormatted("   Some Movie (2021-10-12)", "Some Movie (2021)");
        checkNormalizedFormatted("   Another Movie (2020)", "Another Movie (2020)");
    }

    @Test
    @DisplayName("Check that configured name trim regex works correctly")
    void checkNameTrimRegexOriginName() {
        checkNormalizedFormatted("Bodyguard-S01-Series.1--BBC-2018-720p-w.subs-x265-HEVC", "Bodyguard");
        checkNormalizedFormatted("1922.1080p.[2017].x264", "1922");
    }

    @Test
    void checkNameWithYear() {
        checkNormalizedFormatted("Beetlejuice.1988.XviD", "Beetlejuice (1988)");
    }

    @Test
    @DisplayName("Check replace of & with 'and' works correctly")
    void checkReplaceOfAndWithOriginName() {
        checkNormalizedFormatted("myMovie & me", "MyMovie And Me");
    }

    @Test
    @DisplayName("Check replace of special chars works correctly")
    void checkReplaceOfSpecialCharsOriginName() {
        checkNormalizedFormatted(" hello__Sai***", "Hello Sai");
    }

    @Test
    @DisplayName("Check trim and spaces are merged correctly")
    void checkTrimAndSpacesAreMergedOriginName() {
        checkNormalizedFormatted("  Gnarly   Feels Move ", "Gnarly Feels Move");
    }

    @Test
    @DisplayName("Check words are capitalized correctly")
    void checkCapitalizeOriginName() {
        checkNormalizedFormatted("myName and sUE", "MyName And SUE");
    }

    @Test
    @DisplayName("Check year is retrieved correctly")
    void checkYearRetrievedOriginName() {
        checkNormalizedFormatted(" hmmm a title in 2022 2019", "Hmmm A Title In 2022 (2019)");
    }
}