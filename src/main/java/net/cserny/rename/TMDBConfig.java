package net.cserny.rename;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigMapping(prefix = "tmdb")
public interface TMDBConfig {

    @WithName("api.key")
    String apiKey();

    @WithName("result.limit")
    int resultLimit();

    @WithName("poster.base")
    String posterBase();
}
