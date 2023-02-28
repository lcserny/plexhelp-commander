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
import javax.ws.rs.Produces;

import java.io.IOException;
import java.nio.file.Path;
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

        List<Path> media = service.findMedia();

        assertEquals(2, media.size());
        assertEquals(video1, media.get(0).toAbsolutePath().toString());
        assertEquals(video3, media.get(1).toAbsolutePath().toString());
    }
}
