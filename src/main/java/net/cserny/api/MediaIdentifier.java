package net.cserny.api;

import net.cserny.fs.LocalPath;

public interface MediaIdentifier {

    boolean isMedia(LocalPath path);
}
