package net.cserny.move.automove;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "automove")
@Getter
@Setter
public class AutoMoveProperties {

    private String cron;
    private int limit;
    private int similarityAccepted;
}
