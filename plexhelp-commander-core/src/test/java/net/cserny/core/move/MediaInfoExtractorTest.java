package net.cserny.core.move;

import org.junit.jupiter.api.Test;

import static net.cserny.generated.MediaFileType.MOVIE;
import static net.cserny.generated.MediaFileType.TV;
import static org.assertj.core.api.Assertions.assertThat;

class MediaInfoExtractorTest {

    @Test
    public void baselineExtractor_movieSegments() {
        String name = "Pretty Name from Renamers";
        String videoName = "My VideoFile.mp4";

        MediaInfoExtractor extractor = new MediaInfoExtractor(name, MOVIE, videoName);

        assertThat(extractor.extractMediaInfo().destinationPathSegments()).containsExactly(name, name + ".mp4");
    }

    @Test
    public void baselineExtractor_tvSegments() {
        String name = "Pretty Name from Renamers";
        String videoName = "My VideoFile S02E03.mp4";

        MediaInfoExtractor extractor = new MediaInfoExtractor(name, TV, videoName);

        assertThat(extractor.extractMediaInfo().destinationPathSegments()).containsExactly(name, "Season 2",  name + " S02E03.mp4");
    }

    @Test
    public void baselineExtractor_groupWithSeason_tvSegments() {
        String name = "Pretty Name from Renamers";
        String videoName = "My VideoFile E07.mp4";

        MediaInfoExtractor extractor = new MediaInfoExtractor(name, 4, TV, videoName);

        assertThat(extractor.extractMediaInfo().destinationPathSegments()).containsExactly(name, "Season 4",  name + " S04E07.mp4");
    }

    @Test
    public void baselineExtractor_seasonInGroupAndInVideoName_tvSegments() {
        String name = "Pretty Name from Renamers";
        String videoName = "My VideoFile S01E09.mp4";

        MediaInfoExtractor extractor = new MediaInfoExtractor(name, 4, TV, videoName);

        assertThat(extractor.extractMediaInfo().destinationPathSegments()).containsExactly(name, "Season 1",  name + " S01E09.mp4");
    }

    @Test
    public void baselineExtractor_noSeasonInGroupAndInVideoName_tvSegments() {
        String name = "Yojouhan Shinwa Taikei";
        String videoName = "[Cleo]Yojouhan_Shinwa_Taikei_-_01_(10bit_BD1080p_x265).mkv";

        MediaInfoExtractor extractor = new MediaInfoExtractor(name, null, TV, videoName);

        assertThat(extractor.extractMediaInfo().destinationPathSegments()).containsExactly("Yojouhan Shinwa Taikei",  "Yojouhan Shinwa Taikei E01.mkv");
    }
}