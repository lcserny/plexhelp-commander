package net.cserny.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "automove")
@Getter
@Setter
public class AutoMoveProperties {

    private String cron;
    private int limit;
    private int similarityAccepted;
}
