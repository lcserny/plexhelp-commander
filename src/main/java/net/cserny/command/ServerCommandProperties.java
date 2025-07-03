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
    private boolean wsl;
}