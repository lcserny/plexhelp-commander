package net.cserny.command;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "server.command")
public class ServerCommandProperties {

    private String name;
    private String listenCron;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getListenCron() {
        return listenCron;
    }

    public void setListenCron(String listenCron) {
        this.listenCron = listenCron;
    }
}
