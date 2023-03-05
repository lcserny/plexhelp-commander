package net.cserny.rename;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.List;

@StaticInitSafe
@ConfigMapping(prefix = "rename")
public interface RenameConfig {

    @WithName("trim.regex")
    List<String> trimRegexList();

    @WithName("similarity.percent")
    int similarityPercent();

    @WithName("max.depth")
    int maxDepth();
}
