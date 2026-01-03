package net.cserny;

import io.restassured.RestAssured;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.search.NoAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({QTorrentTestConfiguration.class, MongoTestConfiguration.class})
@ActiveProfiles("test")
@AutoConfigureDataMongo
@Testcontainers
public abstract class BaseIntegrationTest {

    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @Autowired
    protected LocalFileService fileService;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

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
