package net.cserny;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.cserny.config.ApplicationConfig;
import net.cserny.core.rename.TmdbWrapper;
import net.cserny.fs.LocalFileService;
import net.cserny.fs.LocalPath;
import net.cserny.fs.NoAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration
@ContextConfiguration(classes = {
        ApplicationConfig.class,
        BaseIntegrationTest.TestConfig.class,
        QTorrentTestConfiguration.class,
        MongoTestConfiguration.class
})
@ActiveProfiles("test")
@AutoConfigureDataMongo
@Testcontainers
public abstract class BaseIntegrationTest {

    @TestConfiguration(proxyBeanMethods = false)
    public static class TestConfig {

        public static final String JIMFS = "jimfs";
        public static final String TMDBMOCK = "tmdbmock";

        @Bean
        @Primary
        @Profile(JIMFS)
        public FileSystem jimFileSystem() {
            return Jimfs.newFileSystem(Configuration.unix());
        }

        @Bean
        @Primary
        @Profile(TMDBMOCK)
        public TmdbWrapper tmdbWrapper() {
            return mock(TmdbWrapper.class);
        }
    }

    @Autowired
    protected LocalFileService fileService;

    public void deleteDirectory(String pathRoot) throws IOException {
        NoAttributes attr = NoAttributes.builder()
                .isDirectory(true)
                .build();

        LocalPath localPath = fileService.toLocalPath(attr, pathRoot);

        if (Files.notExists(localPath.path())) {
            return;
        }

        Files.walkFileTree(localPath.path(), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) throw exc;
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public LocalPath createDirectories(String pathRoot, String... others) throws IOException {
        NoAttributes attr = NoAttributes.builder()
                .isDirectory(true)
                .build();

        LocalPath localPath = fileService.toLocalPath(attr, pathRoot, others);
        Files.createDirectories(localPath.path());
        return localPath;
    }

    public LocalPath createFile(String pathRoot, String... others) throws IOException {
        NoAttributes attr = NoAttributes.builder()
                .isRegularFile(true)
                .build();

        return createFile(attr, pathRoot, others);
    }

    public LocalPath createFile(BasicFileAttributes attr, String pathRoot, String... others) throws IOException {
        LocalPath localPath = switch (attr) {
            case null -> fileService.toLocalPath(pathRoot, others);
            default -> fileService.toLocalPath(attr, pathRoot, others);
        };

        if (localPath.path().getParent() != null) {
            Files.createDirectories(localPath.path().getParent());
        }
        Files.createFile(localPath.path());
        return localPath;
    }

    public LocalPath createFile(int size, String pathRoot, String... others) throws IOException {
        NoAttributes attr = NoAttributes.builder()
                .size(size)
                .isRegularFile(true)
                .build();

        LocalPath localPath = createFile(attr, pathRoot, others);
        Files.write(localPath.path(), new byte[size]);

        return localPath;
    }
}
