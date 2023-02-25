package net.cserny.search;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

@ApplicationScoped
public class LocalMediaSearchService {

    private final FileSystem fileSystem;

    public LocalMediaSearchService() {
        this.fileSystem = FileSystems.getDefault();
    }

    public LocalMediaSearchService(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
