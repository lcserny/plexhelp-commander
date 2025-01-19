package net.cserny.filesystem;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "filesystem")
public class FilesystemProperties {

    private String downloadsPath;
    private String moviesPath;
    private String tvPath;
    private CacheProperties cache;

    @Getter
    @Setter
    public static class CacheProperties {

        private boolean enabled;
        private boolean loggerEnabled;
        private long initialDelayMs;
        private long cronMs;
    }
}
