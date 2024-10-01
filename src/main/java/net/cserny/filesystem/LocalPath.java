package net.cserny.filesystem;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public record LocalPath(Path path, BasicFileAttributes attributes) implements Comparable<LocalPath> {

    @Override
    public String toString() {
        return path.toString();
    }

    @Override
    public int compareTo(LocalPath o) {
        return this.path().compareTo(o.path());
    }
}
