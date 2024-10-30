package net.cserny.move;

import net.cserny.filesystem.LocalPath;
import net.cserny.generated.MediaFileType;

public record SubsMoveOperation(LocalPath subsSrc, LocalPath subsDest, MediaFileType type) {
}
