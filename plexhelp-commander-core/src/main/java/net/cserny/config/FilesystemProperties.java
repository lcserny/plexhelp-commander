package net.cserny.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "filesystem")
public class FilesystemProperties {

    private String downloadsPath;
    private String moviesPath;
    private String tvPath;
    private CacheProperties cache;

    @Getter
    @Setter
    public static class CacheProperties {

        private String cron;
    }
}
