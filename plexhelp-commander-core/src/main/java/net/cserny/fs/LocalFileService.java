package net.cserny.fs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.LocalPathHandler;
import net.cserny.api.WalkOptions;
import net.cserny.api.dto.LocalPath;
import net.cserny.support.Features;
import org.springframework.stereotype.Component;
import org.togglz.core.manager.FeatureManager;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static net.cserny.api.WalkOptions.ONLY_FILES;

@RequiredArgsConstructor
@Component
@Slf4j
public class LocalFileService implements LocalPathHandler {

    private final FeatureManager featureManager;
    private final CachedLocalPathProvider cachedLocalPathProvider;

    @Getter
    private final FileSystem fileSystem;

    public LocalPath toLocalPath(BasicFileAttributes attributes, String root, String... segments) {
        Path path = fileSystem.getPath(root, segments);
        return toLocalPath(path, attributes);
    }

    @Override
    public LocalPath toLocalPath(String root, String... segments) {
        Path path = fileSystem.getPath(root, segments);
        return toLocalPath(path, null);
    }

    public LocalPath toLocalPath(Path path) {
        return toLocalPath(path, null);
    }

    public LocalPath toLocalPath(Path path, BasicFileAttributes attr) {
        if (featureManager.isActive(Features.FILESYSTEM_CACHE)) {
            return cachedLocalPathProvider.toLocalPath(path, attr, this::getRealAttributes);
        } else {
            if (attr == null) {
                attr = getRealAttributes(path);
            }
            return new LocalPath(path, attr);
        }
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

    @Override
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

    @Override
    public void createDirectories(LocalPath path) throws IOException {
        if (path.attributes().isDirectory()) {
            Files.createDirectories(path.path());
        } else {
            Files.createDirectories(path.path().getParent());
        }
    }

    public boolean move(LocalPath source, LocalPath dest) throws IOException {
        createDirectories(dest);

        if (Files.notExists(dest.path())) {
            Files.move(source.path(), dest.path(), StandardCopyOption.ATOMIC_MOVE);
            return true;
        }

        log.warn("Moving skipped, destination file already present {}", dest.path());

        return false;
    }

    public List<LocalPath> walk(LocalPath path, int maxDepthFromPath, List<String> excludePaths) throws IOException {
        return walk(path, maxDepthFromPath, excludePaths, ONLY_FILES);
    }

    public List<LocalPath> walk(LocalPath path, int maxDepthFromPath) throws IOException {
        return walk(path, maxDepthFromPath, ONLY_FILES);
    }

    @Override
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

        ExcludingFileVisitor excludingFileVisitor = new ExcludingFileVisitor(excludePaths, maxDepthFromPath, options);
        Files.walkFileTree(path.path(), excludingFileVisitor);
        return excludingFileVisitor.getAcceptedPaths().stream()
                .map(this::toLocalPath)
                .toList();
    }
}
