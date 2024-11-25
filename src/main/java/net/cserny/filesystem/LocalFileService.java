package net.cserny.filesystem;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.cserny.LRUCache;
import net.cserny.search.NoAttributes;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
@Service
public class LocalFileService {

    private static final int CACHE_LIMIT = 10000;

    @Getter
    private final LRUCache<String, BasicFileAttributes> fileAttrCache = new LRUCache<>(CACHE_LIMIT);

    protected FileSystem fileSystem = FileSystems.getDefault();

    public LocalPath toLocalPath(BasicFileAttributes attributes, String root, String... segments) {
        Path path = fileSystem.getPath(root, segments);
        return toLocalPath(path, attributes);
    }

    public LocalPath toLocalPath(String root, String... segments) {
        Path path = fileSystem.getPath(root, segments);
        return toLocalPath(path, null);
    }

    public LocalPath toLocalPath(Path path) {
        return toLocalPath(path, null);
    }

    public LocalPath toLocalPath(Path path, BasicFileAttributes attr) {
        String key = path.toAbsolutePath().toString();

        if (attr == null) {
            attr = fileAttrCache.get(key);
            if (attr == null) {
                attr = getRealAttributes(path);
            }
        }

        fileAttrCache.put(key, attr);

        return new LocalPath(path, attr);
    }

    private BasicFileAttributes getRealAttributes(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class);
        } catch (FileNotFoundException ignore) {
        } catch (IOException e) {
            log.warn("Could not determine attributes of file}", e);
        }
        return new NoAttributes();
    }

    public void delete(LocalPath path) throws IOException {
        Files.delete(path.path());
    }

    public boolean exists(LocalPath path) {
        return Files.exists(path.path());
    }

    public void deleteDirectory(LocalPath folder) throws IOException {
        Files.walkFileTree(folder.path(), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void move(LocalPath source, LocalPath dest) throws IOException {
        if (dest.path().getParent() != null) {
            Files.createDirectories(dest.path().getParent());
        }
        Files.move(source.path(), dest.path());
    }

    public List<LocalPath> walk(LocalPath path, int maxDepthFromPath) throws IOException {
        return walk(path, maxDepthFromPath, WalkOptions.ONLY_FILES);
    }

    public List<LocalPath> walk(LocalPath path, int maxDepthFromPath, WalkOptions options) throws IOException {
        if (!path.attributes().isDirectory()) {
            throw new NotDirectoryException(path.path().toString());
        }

        if (maxDepthFromPath < 1) {
            throw new IllegalArgumentException("Max depth passed cannot be lower than 1");
        }

        try (Stream<Path> walkStream = Files.walk(path.path(), maxDepthFromPath, FileVisitOption.FOLLOW_LINKS)) {
            return walkStream.parallel()
                    .map(this::toLocalPath)
                    .filter(localPath -> options.getFilter().test(localPath))
                    .toList();
        }
    }

    @Getter
    public enum WalkOptions {

        ONLY_DIRECTORIES(lp -> lp.attributes().isDirectory()),
        ONLY_FILES(lp -> lp.attributes().isRegularFile());

        private final Predicate<LocalPath> filter;

        WalkOptions(Predicate<LocalPath> filter) {
            this.filter = filter;
        }
    }
}
