package net.cserny;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;

public abstract class AbstractInMemoryFileService {

    @Autowired
    public LocalFileService fileService;

    @BeforeEach
    public void setup() {
        fileService.fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    public LocalPath createDirectories(String somePath) throws IOException {
        LocalPath localPath = fileService.toLocalPath(somePath);
        Files.createDirectories(localPath.path());
        return localPath;
    }

    public LocalPath createFile(String somePath) throws IOException {
        LocalPath localPath = fileService.toLocalPath(somePath);
        if (localPath.path().getParent() != null) {
            Files.createDirectories(localPath.path().getParent());
        }
        Files.createFile(localPath.path());
        return localPath;
    }

    public LocalPath createFile(String somePath, int size) throws IOException {
        LocalPath localPath = createFile(somePath);
        Files.write(localPath.path(), new byte[size]);
        return localPath;
    }
}
