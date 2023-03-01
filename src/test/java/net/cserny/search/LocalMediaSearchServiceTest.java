package net.cserny.search;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.io.IOException;
import java.util.List;

import static net.cserny.filesystem.FileCreator.createFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class LocalMediaSearchServiceTest {

    @Inject
    LocalFileService fileService;

    @Inject
    LocalMediaSearchService service;

    @Inject
    FilesystemConfig filesystemConfig;

    @Inject
    SearchConfig searchConfig;

    @BeforeEach
    public void setup() {
        fileService.fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    // TODO: add tests for these scenarios
    /*
    [
      {
        "path": "/downloads/some movie folder",
        "name": "some movie folder", // this is showed in UI, used by rename
        "videos": [ // also shown in UI under, but you can't change these individually
          "/video1.mp4"
        ]
      },
      {
        "path": "/downloads/some tv folder",
        "name": "some tv folder",
        "videos": [ // used by move, just join <path> to them (separator already appended)
          "/video1.mp4",
          "/video2.mp4",
          "/video3.mp4",
        ]
      },
      {
        "path": "/downloads/some nested folder", // easier to delete
        "name": "some nested folder", // notice the nested structure
        "videos": [
          "/another folder/video1.mp4",
          "/another folder/video2.mp4"
        ]
      },
      {
        "path": "/downloads", // notice no parent folder
        "name": "", // notice name is empty, rename service should generate from file
        "videos": [
          "/video5.mp4",
        ]
      },
    ]
     */
    @Test
    @DisplayName("Check search finds correct media")
    public void checkSearchFindsCorrectMedia() throws IOException {
        String downloadPath = filesystemConfig.downloadsPath();
        String video1 = downloadPath + "/video1.mp4";
        createFile(this.fileService, video1, 6);
        String video2 = downloadPath + "/" + searchConfig.excludePaths().get(0) + "/video2.mp4";
        createFile(this.fileService, video2, 6);
        String video3 = downloadPath + "/video3.mp4";
        createFile(this.fileService, video3, 6);
        String video4 = downloadPath + "/video4.mp4";
        createFile(this.fileService, video4, 1);

        List<MediaFile> media = service.findMedia();

        assertEquals(2, media.size());
        assertEquals(video1, media.get(0).path());
        assertEquals(video3, media.get(1).path());
    }
}
