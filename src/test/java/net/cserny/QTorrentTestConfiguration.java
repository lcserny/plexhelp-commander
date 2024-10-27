package net.cserny;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static java.lang.String.format;

@Configuration
public class QTorrentTestConfiguration {

    public static final int MAPPED_PORT = 8080;

    @Container
    public static GenericContainer container = new GenericContainer("linuxserver/qbittorrent:4.5.2")
            .withExposedPorts(MAPPED_PORT)
            .withFileSystemBind(
                    Paths.get("src", "test", "resources", "qBittorrent.conf").toString(),
                    "/config/qBittorrent/qBittorrent.conf",
                    BindMode.READ_WRITE)
            .withStartupTimeout(Duration.of(10, ChronoUnit.SECONDS))
            .waitingFor(Wait.forLogMessage(".*\\[ls\\.io\\-init\\] done\\..*\\n", 1));

    static {
        container.start();
        System.setProperty("torrent.webui.baseUrl", format("http://%s:%d", container.getHost(), container.getMappedPort(MAPPED_PORT)));
    }
}
