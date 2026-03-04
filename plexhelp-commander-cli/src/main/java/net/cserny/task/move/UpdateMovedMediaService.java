package net.cserny.task.move;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.*;
import net.cserny.generated.MediaFileType;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UpdateMovedMediaService {

    private final MediaMover mediaMoveService;
    private final LocalPathHandler localPathHandler;
    private final MediaIdentifier mediaIdentifier;
    private final RenameSearcher externalRenameSearcher;

    public void updateMovedMedia(String rootMediaPath, MediaFileType mediaFileType) {
        /**
         * TODO
         * walk rootMediaPath
         * identify media
         * for each media file
         *     use mediaMover to check if movedMedia exists already,
         *         if yes, log and skip
         *     extract info needed
         *     get external mediaDescriptions from nameProducer with externalSearcher
         *     persist movedMedia using mediaMover
         */
    }
}
