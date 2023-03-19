package net.cserny.move;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import net.cserny.AbstractInMemoryFileService;
import net.cserny.filesystem.FilesystemConfig;
import net.cserny.rename.MediaFileType;
import net.cserny.rename.MediaRenameOrigin;
import net.cserny.search.MediaFileGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class MediaMoveResourceTest extends AbstractInMemoryFileService {

    @Inject
    FilesystemConfig filesystemConfig;

    @BeforeEach
    public void init() throws IOException {
        createDirectories(filesystemConfig.downloadsPath());
        createDirectories(filesystemConfig.tvShowsPath());
        createDirectories(filesystemConfig.moviesPath());
    }

    @Test
    @DisplayName("Check endpoint moves media correctly")
    public void testMoveCorrectly() throws IOException {
        String path = filesystemConfig.downloadsPath() + "/My Movie";
        String name = "MY Movee";
        String video = "file.mp4";

        createFile(path + "/" + video, 6);

        MediaFileGroup fileGroup = new MediaFileGroup(path, name, List.of(video));
        MediaMoveRequest request = new MediaMoveRequest(fileGroup, MediaFileType.MOVIE);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/media-moves")
                .then()
                .statusCode(200)
                .body("$.size()", is(0));
    }
}