package net.cserny.filesystem;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.cserny.search.NoAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public abstract class AbstractInMemoryFileService {

    @Autowired
    protected LocalFileService fileService;

    @BeforeEach
    public void setup() {
        fileService.fileSystem = Jimfs.newFileSystem(Configuration.unix());
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
