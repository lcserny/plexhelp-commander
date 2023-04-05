package net.cserny.rename;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigMapping(prefix = "online")
public interface OnlineConfig {

    @WithName("result.limit")
    int resultLimit();

    @WithName("poster.base")
    String posterBase();
}
