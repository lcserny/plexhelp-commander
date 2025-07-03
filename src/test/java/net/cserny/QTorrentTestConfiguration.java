package net.cserny;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static java.lang.String.format;

@TestConfiguration(proxyBeanMethods = false)
public class QTorrentTestConfiguration {

    public static final int MAPPED_PORT = 8080;

    private static final String WEBUI_BASE_KEY = "torrent.webui.baseUrl";

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
        System.setProperty(WEBUI_BASE_KEY, buildUrl(container));
    }

    @Bean
    public GenericContainer qtorrentContainer() {
        return container;
    }

    @DynamicPropertySource
    static void setQTorrentProperties(DynamicPropertyRegistry registry) {
        registry.add(WEBUI_BASE_KEY, () -> buildUrl(container));
    }

    private static String buildUrl(GenericContainer container) {
        return format("http://%s:%d", container.getHost(), container.getMappedPort(MAPPED_PORT));
    }
}
