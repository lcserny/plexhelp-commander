package net.cserny.filesystem;

import com.google.common.util.concurrent.Striped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.support.Features;
import net.cserny.search.NoAttributes;
import org.springframework.stereotype.Component;
import org.togglz.core.manager.FeatureManager;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static net.cserny.filesystem.ExcludingFileVisitor.WalkOptions.ONLY_FILES;

@RequiredArgsConstructor
@Component
@Slf4j
public class LocalFileService {

    private static final int nrLocks = 32;

    private final Striped<Lock> moveLocks = Striped.lock(nrLocks);

    private final FeatureManager featureManager;
    private final CachedLocalPathProvider cachedLocalPathProvider;
    private final FileSystem fileSystem;

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

    public boolean checkedMove(LocalPath source, LocalPath dest) throws IOException {
        String key = dest.path().toString();
        Lock lock = moveLocks.get(key);

        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    if (dest.path().getParent() != null) {
                        Files.createDirectories(dest.path().getParent());
                    }

                    if (Files.notExists(dest.path())) {
                        Files.move(source.path(), dest.path(), StandardCopyOption.ATOMIC_MOVE);
                        return true;
                    } else {
                        throw new FileAlreadyExistsException("Destination file '" + key + "' already exists");
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                throw new IOException("Lock timed out trying to move file: " + key);
            }
        } catch (InterruptedException e) {
            log.debug(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }

        return false;
    }

    public List<LocalPath> walk(LocalPath path, int maxDepthFromPath, List<String> excludePaths) throws IOException {
        return walk(path, maxDepthFromPath, excludePaths, ONLY_FILES);
    }

    public List<LocalPath> walk(LocalPath path, int maxDepthFromPath) throws IOException {
        return walk(path, maxDepthFromPath, ONLY_FILES);
    }

    public List<LocalPath> walk(LocalPath path, int maxDepthFromPath, ExcludingFileVisitor.WalkOptions options) throws IOException {
        return walk(path, maxDepthFromPath, List.of(), options);
    }

    public List<LocalPath> walk(LocalPath path, int maxDepthFromPath, List<String> excludePaths, ExcludingFileVisitor.WalkOptions options) throws IOException {
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
