package net.cserny.core.move.automove;

import net.cserny.api.*;

public interface AutomoveSupport extends
        DownloadedMediaManipulator,
        NameNormalizer,
        MediaFileGroupGenerator,
        NameOptionsProducer,
        MediaMover {
}
