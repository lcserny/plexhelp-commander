package net.cserny.move;

import net.cserny.filesystem.LocalPath;
import net.cserny.rename.MediaFileType;

public record SubsMoveOperation(LocalPath subsSrc, LocalPath subsDest, MediaFileType type) {
}
