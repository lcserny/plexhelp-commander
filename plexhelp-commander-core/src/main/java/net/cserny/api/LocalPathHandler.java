package net.cserny.api;

import net.cserny.fs.LocalPath;

import java.io.IOException;

public interface LocalPathHandler {

    LocalPath toLocalPath(String root, String... segments);
    void createDirectories(LocalPath path) throws IOException;
}
