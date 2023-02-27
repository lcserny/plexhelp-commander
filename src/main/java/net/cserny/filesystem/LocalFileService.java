package net.cserny.filesystem;

import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class LocalFileService {

    private static final Logger LOGGER = Logger.getLogger(LocalFileService.class);

    private FileSystem fileSystem;

    @PostConstruct
    public void init() {
        this.fileSystem = FileSystems.getDefault();
    }

    public void setFileSystem(FileSystem fileSystem) {
        LOGGER.info("Overriding fileSystem with " + fileSystem.getClass().getSimpleName());
        this.fileSystem = fileSystem;
    }

    public LocalPath produceLocalPath(String root, String... segments) {
        return new LocalPath(fileSystem.getPath(root, segments));
    }

    public void delete(LocalPath path) throws IOException {
        Files.delete(path.path());
    }

    public void move(LocalPath source, LocalPath dest) throws IOException {
        if (dest.path().getParent() != null) {
            Files.createDirectories(dest.path().getParent());
        }
        Files.move(source.path(), dest.path());
    }

    public List<Path> walk(LocalPath path, int maxDepth) throws IOException {
        List<Path> files;
        try (Stream<Path> walkStream = Files.walk(path.path(), maxDepth, FileVisitOption.FOLLOW_LINKS)) {
            files = walkStream.filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
        return files;
    }
}
