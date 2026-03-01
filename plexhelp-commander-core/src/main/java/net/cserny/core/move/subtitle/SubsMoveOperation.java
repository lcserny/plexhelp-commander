package net.cserny.core.move.subtitle;

import net.cserny.api.dto.LocalPath;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;

public record SubsMoveOperation(LocalPath subsSrc, String destRoot, MediaFileType type, MediaFileGroup group) {
}
