package net.cserny.api;

import net.cserny.api.dto.LocalPath;

public interface MediaIdentifier {

    boolean isMedia(LocalPath path);
    boolean isSubtitle(LocalPath path);
}
