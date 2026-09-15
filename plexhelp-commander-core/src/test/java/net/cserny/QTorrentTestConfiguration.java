package net.cserny;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static java.lang.String.format;

@TestConfiguration(proxyBeanMethods = false)
public class QTorrentTestConfiguration {

    public static final int MAPPED_PORT = 8080;

    private static final String WEBUI_BASE_KEY = "torrent.webui.baseUrl";

    @Bean
    GenericContainer<?> qtorrentContainer() {
        return new GenericContainer<>(DockerImageName.parse("linuxserver/qbittorrent:4.5.2"))
                .withExposedPorts(MAPPED_PORT)
                .withFileSystemBind(
                        qBittorrentConfPath(),
                        "/config/qBittorrent/qBittorrent.conf",
                        BindMode.READ_WRITE)
                .withStartupTimeout(Duration.of(10, ChronoUnit.SECONDS))
                .waitingFor(Wait.forLogMessage(".*\\[ls\\.io\\-init\\] done\\..*\\n", 1));
    }

    @Bean
    DynamicPropertyRegistrar qtorrentProperties(GenericContainer<?> qtorrentContainer) {
        return registry -> registry.add(WEBUI_BASE_KEY, () -> buildUrl(qtorrentContainer));
    }

    private static String qBittorrentConfPath() {
        Path moduleLocal = Path.of("src", "test", "resources", "qBittorrent.conf");
        if (Files.exists(moduleLocal)) {
            return moduleLocal.toAbsolutePath().toString();
        }
        URL resource = QTorrentTestConfiguration.class.getResource("/qBittorrent.conf");
        if (resource != null && "file".equalsIgnoreCase(resource.getProtocol())) {
            try {
                return Path.of(resource.toURI()).toString();
            } catch (URISyntaxException | IllegalArgumentException ignored) {
                // fall through to module-local path
            }
        }
        return moduleLocal.toAbsolutePath().toString();
    }

    private static String buildUrl(GenericContainer<?> container) {
        return format("http://%s:%d", container.getHost(), container.getMappedPort(MAPPED_PORT));
    }
}
