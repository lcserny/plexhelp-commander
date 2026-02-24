package net.cserny.core.move.subtitle;

import net.cserny.generated.MediaMoveError;

public sealed interface SubtitleMoveResult {

    record Success() implements  SubtitleMoveResult {}
    record Failure(MediaMoveError error) implements  SubtitleMoveResult {}
}
