package net.cserny.api.dto;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static net.cserny.api.dto.MediaInfo.SEASON_SUBSTR;

public record LocalPath(Path path, BasicFileAttributes attributes) implements Comparable<LocalPath> {

    /**
     * @return the path of this media's parent, handling correctly a TV show that has a Season middle directory
     */
    public Path getMediaParent() {
        Path parent = path().getParent();
        if (parent.getFileName().toString().contains(SEASON_SUBSTR)) {
            return parent.getParent();
        }
        return parent;
    }

    @Override
    public String toString() {
        return path.toString();
    }

    @Override
    public int compareTo(LocalPath o) {
        return this.path().compareTo(o.path());
    }
}
