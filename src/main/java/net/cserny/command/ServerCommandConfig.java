package net.cserny.command;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigMapping(prefix = "server.command")
public interface ServerCommandConfig {

    String name();

    @WithName("listen.interval")
    String listenInterval();
}
