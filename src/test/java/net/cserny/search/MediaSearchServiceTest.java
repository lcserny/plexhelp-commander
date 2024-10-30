package net.cserny.search;

import net.cserny.filesystem.AbstractInMemoryFileService;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.generated.MediaFileGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest({
        "server.command.name=test-server",
        "server.command.listen-cron=disabled",
        "search.video-min-size-bytes=5",
        "search.exclude-paths[0]=Excluded Folder 1"
})
@ContextConfiguration(classes = {
        MediaSearchService.class,
        FilesystemProperties.class,
        SearchProperties.class,
        LocalFileService.class,
        MediaIdentificationService.class
})
@EnableAutoConfiguration(exclude = MongoAutoConfiguration.class)
public class MediaSearchServiceTest extends AbstractInMemoryFileService {

    @Autowired
    MediaSearchService service;

    @Autowired
    FilesystemProperties filesystemConfig;

    @Autowired
    SearchProperties searchConfig;

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
        String downloadPath = filesystemConfig.getDownloadsPath();
        String video1 = downloadPath + "/video1.mp4";
        createFile(6, video1);
        String video2 = downloadPath + "/" + searchConfig.getExcludePaths().getFirst() + "/video2.mp4";
        createFile(6, video2);
        String video3 = downloadPath + "/video3.mp4";
        createFile(6, video3);
        String video4 = downloadPath + "/video4.mp4";
        createFile(1, video4);
        String video5 = downloadPath + "/some tvShow/video3.mp4";
        createFile(6, video5);
        String video6 = downloadPath + "/some tvShow/video1.mp4";
        createFile(6, video6);
        String video7 = downloadPath + "/some tvShow/video5.mp4";
        createFile(6, video7);

        List<MediaFileGroup> media = service.findMedia();

        assertEquals(3, media.size());
        assertEquals(downloadPath, media.get(0).getPath());
        assertEquals("video1", media.get(0).getName());
        assertEquals(downloadPath + "/some tvShow", media.get(2).getPath());
        assertEquals("some tvShow", media.get(2).getName());
        assertEquals("video1.mp4", media.get(2).getVideos().get(0));
        assertEquals("video3.mp4", media.get(2).getVideos().get(1));
        assertEquals("video5.mp4", media.get(2).getVideos().get(2));
    }
}
