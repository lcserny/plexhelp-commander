package net.cserny.filesystem;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigMapping(prefix = "filesystem")
public interface FilesystemConfig {

    @WithName("downloads.path")
    String downloadsPath();

    @WithName("movies.path")
    String moviesPath();

    @WithName("tv.path")
    String tvShowsPath();
}
