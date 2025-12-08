package net.cserny.move;

import net.cserny.generated.MediaFileType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MediaInfoExtractorTest {

    @Test
    public void baselineExtractor_movieSegments() {
        String name = "Pretty Name from Renamers";
        String videoName = "My VideoFile.mp4";

        MediaInfoExtractor extractor = new MediaInfoExtractor(name, MediaFileType.MOVIE, videoName);

        assertThat(extractor.extractMediaInfo().destinationPathSegments()).containsExactly(name, name + ".mp4");
    }

    @Test
    public void baselineExtractor_tvSegments() {
        String name = "Pretty Name from Renamers";
        String videoName = "My VideoFile S02E03.mp4";

        MediaInfoExtractor extractor = new MediaInfoExtractor(name, MediaFileType.TV, videoName);

        assertThat(extractor.extractMediaInfo().destinationPathSegments()).containsExactly(name, "Season 2",  name + " S02E03.mp4");
    }

    @Test
    public void baselineExtractor_groupWithSeason_tvSegments() {
        String name = "Pretty Name from Renamers";
        String videoName = "My VideoFile E07.mp4";

        MediaInfoExtractor extractor = new MediaInfoExtractor(name, 4, MediaFileType.TV, videoName);

        assertThat(extractor.extractMediaInfo().destinationPathSegments()).containsExactly(name, "Season 4",  name + " S04E07.mp4");
    }

    @Test
    public void baselineExtractor_seasonInGroupAndInVideoName_tvSegments() {
        String name = "Pretty Name from Renamers";
        String videoName = "My VideoFile S01E09.mp4";

        MediaInfoExtractor extractor = new MediaInfoExtractor(name, 4, MediaFileType.TV, videoName);

        assertThat(extractor.extractMediaInfo().destinationPathSegments()).containsExactly(name, "Season 1",  name + " S01E09.mp4");
    }
}