package net.cserny.filesystem;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "filesystem")
public class FilesystemProperties {

    private String downloadsPath;
    private String moviesPath;
    private String tvPath;

    public String getDownloadsPath() {
        return downloadsPath;
    }

    public void setDownloadsPath(String downloadsPath) {
        this.downloadsPath = downloadsPath;
    }

    public String getMoviesPath() {
        return moviesPath;
    }

    public void setMoviesPath(String moviesPath) {
        this.moviesPath = moviesPath;
    }

    public String getTvPath() {
        return tvPath;
    }

    public void setTvPath(String tvPath) {
        this.tvPath = tvPath;
    }
}
