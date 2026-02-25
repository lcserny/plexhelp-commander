package net.cserny.api;

import net.cserny.fs.LocalPath;

import java.io.IOException;

public interface DirectoryCreator {

    void createDirectories(LocalPath path) throws IOException;
}
