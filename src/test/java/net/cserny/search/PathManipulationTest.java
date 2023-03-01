package net.cserny.search;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PathManipulationTest {

    private static final Logger LOGGER = Logger.getLogger(PathManipulationTest.class.getSimpleName());

    @Test
    public void substring1() {
        String downloadsPath = "/downloads";
        String path = downloadsPath;

        Path videoPath = Paths.get("/downloads/some nested folder/another one/video5.mp4");

        String name = videoPath.toString();
        name = name.substring(downloadsPath.length());
        name = name.substring(0, name.indexOf(videoPath.getFileName().toString()));
        if (!name.matches(Pattern.quote(File.separator) + "*")) {
            name = name.substring(0,
                    name.indexOf(File.separator, name.indexOf(File.separator) + 1));
            name = name.replaceAll(Pattern.quote(File.separator), "");
            path = Paths.get(downloadsPath, name).toString();
        } else {
            name = null;
        }

        String remainingVideoPath = videoPath.toString().substring(path.length());

        assertEquals("/downloads/some nested folder", path);
        assertEquals("some nested folder", name);
        assertEquals("/another one/video5.mp4", remainingVideoPath);
    }

    @Test
    public void substring2() {
        String downloadsPath = "/downloads";
        String path = downloadsPath;

        Path videoPath = Paths.get("/downloads/video5.mp4");

        String name = videoPath.toString();
        name = name.substring(downloadsPath.length());
        name = name.substring(0, name.indexOf(videoPath.getFileName().toString()));
        if (!name.matches(Pattern.quote(File.separator) + "*")) {
            name = name.substring(0,
                    name.indexOf(File.separator, name.indexOf(File.separator) + 1));
            name = name.replaceAll(Pattern.quote(File.separator), "");
            path = Paths.get(downloadsPath, name).toString();
        } else {
            name = null;
        }

        String remainingVideoPath = videoPath.toString().substring(path.length());

        assertEquals("/downloads", path);
        assertNull(name);
        assertEquals("/video5.mp4", remainingVideoPath);
    }
}
