package net.cserny.api;

import net.cserny.fs.LocalPath;

public interface LocalPathConverter {

    LocalPath toLocalPath(String root, String... segments);
}
