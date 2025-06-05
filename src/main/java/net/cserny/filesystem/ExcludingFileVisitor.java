package net.cserny.filesystem;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Slf4j
public class ExcludingFileVisitor implements FileVisitor<Path> {

    @Getter
    private final Set<Path> acceptedPaths = new TreeSet<>();

    private int currentDepth;
    private final int maxWalkDepth;

    private final WalkOptions options;

    private final List<String> excludedPaths;

    public ExcludingFileVisitor(List<String> excludedPaths, int maxWalkDepth, WalkOptions options) {
        this.excludedPaths = excludedPaths;
        this.maxWalkDepth = maxWalkDepth;
        this.currentDepth = 0;
        this.options = options;
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

        if (this.options == WalkOptions.ONLY_DIRECTORIES) {
            this.acceptedPaths.add(dir);
        }

        currentDepth++;
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (this.options == WalkOptions.ONLY_FILES) {
            this.acceptedPaths.add(file);
        }
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

    @Getter
    public enum WalkOptions {

        ONLY_DIRECTORIES,
        ONLY_FILES;
    }
}
