package net.cserny.api;

import net.cserny.api.dto.LocalPath;

import java.io.IOException;
import java.util.List;

public interface LocalPathHandler {

    LocalPath toLocalPath(String root, String... segments);

    void createDirectories(LocalPath path) throws IOException;

    void deleteDirectory(LocalPath folder) throws IOException;

    List<LocalPath> walk(LocalPath path, int maxDepthFromPath, WalkOptions options) throws IOException;
}
