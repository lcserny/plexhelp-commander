package net.cserny.core.move.automove;

import net.cserny.NameOptionsProducer;
import net.cserny.api.DownloadedMediaManipulator;
import net.cserny.api.MediaFileGroupGenerator;
import net.cserny.api.MediaMover;
import net.cserny.api.NameNormalizer;

public interface AutomoveSupport extends
        DownloadedMediaManipulator,
        NameNormalizer,
        MediaFileGroupGenerator,
        NameOptionsProducer,
        MediaMover {
}
