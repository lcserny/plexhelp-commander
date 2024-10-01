package net.cserny.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoAttributes implements BasicFileAttributes {

    // add more props to override in builder if needed
    private long size;
    private boolean isDirectory;
    private boolean isRegularFile;

    @Override
    public FileTime lastModifiedTime() {
        return null;
    }

    @Override
    public FileTime lastAccessTime() {
        return null;
    }

    @Override
    public FileTime creationTime() {
        return null;
    }

    @Override
    public boolean isRegularFile() {
        return this.isRegularFile;
    }

    @Override
    public boolean isDirectory() {
        return this.isDirectory;
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public Object fileKey() {
        return null;
    }
}
