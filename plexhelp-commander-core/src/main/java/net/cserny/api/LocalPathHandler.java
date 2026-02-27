package net.cserny.api;

import net.cserny.fs.LocalPath;

import java.io.IOException;
import java.util.List;

public interface LocalPathHandler {

    LocalPath toLocalPath(String root, String... segments);

    void createDirectories(LocalPath path) throws IOException;

    List<LocalPath> walk(LocalPath path, int maxDepthFromPath) throws IOException;

}
