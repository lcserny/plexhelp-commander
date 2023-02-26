package net.cserny.filesystem;

import javax.enterprise.context.ApplicationScoped;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

@ApplicationScoped
public class LocalFileService {

    private final FileSystem fileSystem;

    public LocalFileService() {
        this.fileSystem = FileSystems.getDefault();
    }

    public LocalFileService(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
