package net.cserny.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "torrent.webui")
public class TorrentProperties {

    private String baseUrl;
    private String username;
    private String password;
    private int readTimeout;
}
