package net.cserny.command;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "server.command")
@Setter
@Getter
public class ServerCommandProperties {

    private String name;
    private String listenCron;
    private WslProperties wsl;
    private SshProperties ssh;

    @Getter
    @Setter
    public static class WslProperties {

        private boolean enabled;
        private String system32Path;
    }

    @Getter
    @Setter
    public static class SshProperties {

        private boolean enabled;
        private String host;
        private int port;
        private String username;
        private String password;
    }
}
