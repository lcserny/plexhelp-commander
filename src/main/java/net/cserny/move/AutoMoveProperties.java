package net.cserny.move;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "automove")
@Getter
@Setter
public class AutoMoveProperties {

    private String cron;
    private int limit;
    private int similarityAccepted;
}
