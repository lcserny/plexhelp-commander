package net.cserny.search;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.List;

@StaticInitSafe
@ConfigMapping(prefix = "search")
public interface SearchConfig {

    @WithName("max.depth")
    int maxDepth();

    @WithName("exclude.paths")
    List<String> excludePaths();

    @WithName("video.min.size.bytes")
    long videoMinSizeInBytes();

    @WithName("video.mime.types")
    List<String> videoMimeTypes();
}
