package net.cserny.move.subtitle;

import net.cserny.filesystem.LocalPath;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;

public record SubsMoveOperation(LocalPath subsSrc, String destRoot, MediaFileType type, MediaFileGroup group) {
}
