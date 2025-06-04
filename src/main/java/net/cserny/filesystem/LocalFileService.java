package net.cserny.filesystem;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.cserny.search.NoAttributes;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
public class LocalFileService {

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
        if (attr == null) {
            attr = getRealAttributes(path);
        }
        return new LocalPath(path, attr);
    }

    protected BasicFileAttributes getRealAttributes(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class);
        } catch (NoSuchFileException ignore) {
        } catch (IOException e) {
            log.warn("Could not determine attributes of file {}", e.getMessage());
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

    public List<LocalPath> walk(LocalPath path, int maxDepthFromPath, List<String> excludePaths) throws IOException {
        return walk(path, maxDepthFromPath, excludePaths, WalkOptions.ONLY_FILES);
    }

    public List<LocalPath> walk(LocalPath path, int maxDepthFromPath) throws IOException {
        return walk(path, maxDepthFromPath, WalkOptions.ONLY_FILES);
    }

    public List<LocalPath> walk(LocalPath path, int maxDepthFromPath, WalkOptions options) throws IOException {
        return walk(path, maxDepthFromPath, List.of(), options);
    }

    public List<LocalPath> walk(LocalPath path, int maxDepthFromPath, List<String> excludePaths, WalkOptions options) throws IOException {
        if (!path.attributes().isDirectory()) {
            throw new NotDirectoryException(path.path().toString());
        }

        if (maxDepthFromPath < 1) {
            throw new IllegalArgumentException("Max depth passed cannot be lower than 1");
        }

        ExcludingFileVisitor excludingFileVisitor = new ExcludingFileVisitor(excludePaths, maxDepthFromPath);
        Files.walkFileTree(path.path(), excludingFileVisitor);
        return excludingFileVisitor.getAcceptedPaths().stream()
                .map(this::toLocalPath)
                .filter(localPath -> options.getFilter().test(localPath))
                .toList();
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
