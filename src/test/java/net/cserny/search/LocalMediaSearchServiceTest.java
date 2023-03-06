package net.cserny.search;

import io.quarkus.test.junit.QuarkusTest;
import net.cserny.AbstractInMemoryFileService;
import net.cserny.filesystem.FilesystemConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class LocalMediaSearchServiceTest extends AbstractInMemoryFileService {

    @Inject
    LocalMediaSearchService service;

    @Inject
    FilesystemConfig filesystemConfig;

    @Inject
    SearchConfig searchConfig;

    /*
    [
      {
        "path": "/downloads/some movie folder",
        "name": "some movie folder", // this is showed in UI, used by rename
        "videos": [ // also shown in UI under, but you can't change these individually
          "video1.mp4"
        ]
      },
      {
        "path": "/downloads/some tv folder",
        "name": "some tv folder",
        "videos": [ // used by move, just resolve <path> to them
          "video1.mp4",
          "video2.mp4",
          "video3.mp4",
        ]
      },
      {
        "path": "/downloads/some nested folder", // easier to delete
        "name": "some nested folder", // notice the nested structure
        "videos": [
          "another folder/video1.mp4",
          "another folder/video2.mp4"
        ]
      },
      {
        "path": "/downloads", // notice no parent folder
        "name": "video5", // notice its generated from file name without extension
        "videos": [
          "video5.mp4",
        ]
      },
    ]
     */
    @Test
    @DisplayName("Check search finds correct media")
    public void checkSearchFindsCorrectMedia() throws IOException {
        String downloadPath = filesystemConfig.downloadsPath();
        String video1 = downloadPath + "/video1.mp4";
        createFile(video1, 6);
        String video2 = downloadPath + "/" + searchConfig.excludePaths().get(0) + "/video2.mp4";
        createFile(video2, 6);
        String video3 = downloadPath + "/video3.mp4";
        createFile(video3, 6);
        String video4 = downloadPath + "/video4.mp4";
        createFile(video4, 1);
        String video5 = downloadPath + "/some tvShow/video3.mp4";
        createFile(video5, 6);
        String video6 = downloadPath + "/some tvShow/video1.mp4";
        createFile(video6, 6);
        String video7 = downloadPath + "/some tvShow/video5.mp4";
        createFile(video7, 6);

        List<MediaFileGroup> media = service.findMedia();

        assertEquals(3, media.size());
        assertEquals(downloadPath, media.get(0).path());
        assertEquals("video1", media.get(0).name());
        assertEquals(downloadPath + "/some tvShow", media.get(2).path());
        assertEquals("some tvShow", media.get(2).name());
        assertEquals("video1.mp4", media.get(2).videos().get(0));
        assertEquals("video3.mp4", media.get(2).videos().get(1));
        assertEquals("video5.mp4", media.get(2).videos().get(2));
    }
}
