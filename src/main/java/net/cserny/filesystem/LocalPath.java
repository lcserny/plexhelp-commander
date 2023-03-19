package net.cserny.filesystem;

import java.nio.file.Path;

public record LocalPath(Path path) {

    @Override
    public String toString() {
        return path.toString();
    }
}
