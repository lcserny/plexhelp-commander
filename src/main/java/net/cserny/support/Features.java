package net.cserny.support;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;

public enum Features implements Feature {

    @EnabledByDefault
    @Label("Automatically move media files downloaded if possible")
    AUTOMOVE,

    @Label("Use a InMemory cache for the filesystem operations")
    FILESYSTEM_CACHE,

    @Label("Log operations on the InMemory cache")
    FILESYSTEM_CACHE_LOGGING;
}
