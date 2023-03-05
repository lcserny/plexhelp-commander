package net.cserny.filesystem;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class LocalFileService {

    public FileSystem fileSystem = FileSystems.getDefault();

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

    public List<Path> walk(LocalPath path, int maxDepthFromPath) throws IOException {
        return walk(path, maxDepthFromPath, WalkOptions.ONLY_FILES);
    }

    public List<Path> walk(LocalPath path, int maxDepthFromPath, WalkOptions options) throws IOException {
        if (!Files.isDirectory(path.path())) {
            throw new NotDirectoryException(path.path().toString());
        }

        if (maxDepthFromPath < 1) {
            throw new IllegalArgumentException("Max depth passed cannot be lower than 1");
        }

        Predicate<Path> optionsPredicate = switch (options) {
            case ONLY_DIRECTORIES -> Files::isRegularFile;
            case ONLY_FILES -> Files::isDirectory;
        };

        List<Path> files;
        try (Stream<Path> walkStream = Files.walk(path.path(), maxDepthFromPath, FileVisitOption.FOLLOW_LINKS)) {
            files = walkStream.filter(optionsPredicate)
                    .collect(Collectors.toList());
        }

        return files;
    }

    public enum WalkOptions {
        ONLY_DIRECTORIES,
        ONLY_FILES;
    }
}
