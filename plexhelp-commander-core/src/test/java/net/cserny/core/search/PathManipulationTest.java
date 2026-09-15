package net.cserny.core.search;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathManipulationTest {

    @Test
    public void substring1() {
        Path downloadsPath = Path.of("/downloads");
        int downloadsPathSegments = downloadsPath.getNameCount();
        Path videoPath = Path.of("/downloads/some nested folder/another one/video5.mp4");
        int videoPathSegments = videoPath.getNameCount();

        Path name = videoPath.subpath(downloadsPathSegments, downloadsPathSegments + 1);
        Path pth = downloadsPath;
        Path remainingFile = name;
        if (videoPathSegments > downloadsPathSegments + 1) {
            pth = downloadsPath.resolve(name);
            remainingFile = videoPath.subpath(downloadsPathSegments + 1, videoPathSegments);
        } else {
            String nameString = name.toString();
            name = Path.of(nameString.substring(0, nameString.lastIndexOf(".")));
        }

        assertEquals("/downloads/some nested folder", pth.toString());
        assertEquals("some nested folder", name.toString());
        assertEquals("another one/video5.mp4", remainingFile.toString());
    }

    @Test
    public void substring2() {
        Path downloadsPath = Path.of("/downloads");
        int downloadsPathSegments = downloadsPath.getNameCount();
        Path videoPath = Path.of("/downloads/video5.mp4");
        int videoPathSegments = videoPath.getNameCount();

        Path name = videoPath.subpath(downloadsPathSegments, downloadsPathSegments + 1);
        Path pth = downloadsPath;
        Path remainingFile = name;
        if (videoPathSegments > downloadsPathSegments + 1) {
            pth = downloadsPath.resolve(name);
            remainingFile = videoPath.subpath(downloadsPathSegments + 1, videoPathSegments);
        } else {
            String nameString = name.toString();
            name = Path.of(nameString.substring(0, nameString.lastIndexOf(".")));
        }

        assertEquals("/downloads", pth.toString());
        assertEquals("video5", name.toString());
        assertEquals("video5.mp4", remainingFile.toString());
    }
}
