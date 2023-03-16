package net.cserny.move;

import net.cserny.filesystem.LocalFileService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SubtitleMover {

    @Inject
    LocalFileService fileService;

    // TODO:
    public List<MediaMoveError> moveSubs(SubsMoveOperation operation) {
        return null;
    }
}
