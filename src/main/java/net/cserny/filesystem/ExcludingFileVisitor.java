package net.cserny.filesystem;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ExcludingFileVisitor implements FileVisitor<Path> {

    @Getter
    private final Set<Path> acceptedPaths = new HashSet<>();

    private int currentDepth;
    private final int maxWalkDepth;

    private final List<String> excludedPaths;

    public ExcludingFileVisitor(List<String> excludedPaths, int maxWalkDepth) {
        this.excludedPaths = excludedPaths;
        this.maxWalkDepth = maxWalkDepth;
        this.currentDepth = 0;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        for (String excludedPath : this.excludedPaths) {
            if (dir.toAbsolutePath().toString().contains(excludedPath)) {
                log.info("Excluding {} from file visit", dir.toAbsolutePath());
                return FileVisitResult.SKIP_SUBTREE;
            }
        }

        if (currentDepth >= maxWalkDepth) {
            log.info("Reached maximum walk depth of {} at {}", maxWalkDepth, dir.toAbsolutePath());
            return FileVisitResult.SKIP_SUBTREE;
        }

        currentDepth++;
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        this.acceptedPaths.add(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        log.warn("Failed to visit file {}: {}", file, exc.getMessage());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        currentDepth--;
        return FileVisitResult.CONTINUE;
    }
}
