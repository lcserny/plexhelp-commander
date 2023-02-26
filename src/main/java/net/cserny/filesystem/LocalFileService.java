package net.cserny.filesystem;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

@ApplicationScoped
public class LocalFileService {

    private FileSystem fileSystem;

    @PostConstruct
    public void init() {
        this.fileSystem = FileSystems.getDefault();
    }

    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    // TODO: delete a path, move a Path, walk a Path
}
