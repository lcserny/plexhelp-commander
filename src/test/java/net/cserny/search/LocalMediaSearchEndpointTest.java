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

import static io.restassured.RestAssured.given;
import static net.cserny.filesystem.FileCreator.createFile;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class LocalMediaSearchEndpointTest {

    @Inject
    FilesystemConfig filesystemConfig;

    @Inject
    SearchConfig searchConfig;

    @Inject
    LocalFileService fileService;

    @BeforeEach
    public void setup() {
        fileService.fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

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

        given()
                .when().get("/api/v1/search/media")
                .then()
                .statusCode(200)
                .body("$.size()", is(2))
                .body("[0].path", is(video1))
                .body("[1].path", is(video3));
    }
}
