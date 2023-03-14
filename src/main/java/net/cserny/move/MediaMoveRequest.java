package net.cserny.move;

import net.cserny.rename.MediaFileType;
import net.cserny.search.MediaFileGroup;

public record MediaMoveRequest(MediaFileGroup fileGroup, MediaFileType type) {
}
