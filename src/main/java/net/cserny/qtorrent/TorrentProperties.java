package net.cserny.qtorrent;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "torrent.webui")
public class TorrentProperties {

    private String baseUrl;
    private String username;
    private String password;
}
