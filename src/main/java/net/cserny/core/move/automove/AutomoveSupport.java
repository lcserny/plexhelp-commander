package net.cserny.core.move.automove;

import net.cserny.NameOptionsProducer;
import net.cserny.api.AutomoveDownloadedManipulator;
import net.cserny.api.MediaFileGroupGenerator;
import net.cserny.api.MediaMover;
import net.cserny.api.NameNormalizer;

public interface AutomoveSupport extends
        AutomoveDownloadedManipulator,
        NameNormalizer,
        MediaFileGroupGenerator,
        NameOptionsProducer,
        MediaMover {
}
