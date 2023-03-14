package net.cserny.move;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.List;

@StaticInitSafe
@ConfigMapping(prefix = "move")
public interface MoveConfig {

    @WithName("restricted.remove.paths")
    List<String> restrictedRemovePaths();
}
