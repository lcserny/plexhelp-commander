package net.cserny.move;

import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.generated.MediaMoveError;
import net.cserny.move.MediaInfoExtractor.MediaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.cserny.CommanderApplication.toOneLineString;
import static net.cserny.filesystem.ExcludingFileVisitor.WalkOptions.ONLY_FILES;

@Service
@Slf4j
public class SubtitleMover {

    @Autowired
    LocalFileService fileService;

    @Autowired
    FilesystemProperties filesystemConfig;

    @Autowired
    MoveProperties moveConfig;

    public List<MediaMoveError> moveSubs(SubsMoveOperation operation) {
        if (operation.subsSrc().path().toString().equals(filesystemConfig.getDownloadsPath())) {
            log.info("Path to move subs is root Downloads path, skipping operation...");
            return Collections.emptyList();
        }

        List<MediaMoveError> errors = new ArrayList<>();

        List<LocalPath> subs;
        try {
            subs = fileService.walk(operation.subsSrc(), moveConfig.getSubsMaxDepth(), ONLY_FILES)
                    .stream().parallel()
                    .filter(this::filterBySubExtension)
                    .toList();
        } catch (IOException e) {
            log.warn("Could not walk subs path: {}", e.getMessage());
            errors.add(new MediaMoveError().mediaPath(operation.subsSrc().path().toString()).error(e.getMessage()));
            return errors;
        }

        if (subs.isEmpty()) {
            log.info("No subs found for media {}", operation.subsSrc());
            return Collections.emptyList();
        }

        log.info("{} type subs found {}", operation.type().toString(), toOneLineString(subs));

        errors.addAll(moveSubs(operation, subs));

        return errors;
    }

    private List<MediaMoveError> moveSubs(SubsMoveOperation operation, List<LocalPath> subs) {
        List<MediaMoveError> errors = new ArrayList<>();

        for (LocalPath sub : subs) {
            LocalPath subSrc = fileService.toLocalPath(sub.toString());

            String subNameOnly = sub.path().getFileName().toString();
            MediaInfoExtractor extractor = new MediaInfoExtractor(operation.group().getName(), operation.group().getSeason(), operation.type(), subNameOnly);
            MediaInfo mediaInfo = extractor.extractMediaInfo();
            LocalPath subDest = fileService.toLocalPath(operation.destRoot(), mediaInfo.destinationPathSegments());

            try {
                log.info("Moving sub {} to {}", subSrc, subDest);
                fileService.move(subSrc, subDest);
            } catch (IOException e) {
                log.warn("Could not move sub: {}", e.getMessage());
                errors.add(new MediaMoveError().mediaPath(subSrc.path().toString()).error(e.getMessage()));
            }
        }

        return errors;
    }

    private boolean filterBySubExtension(LocalPath subPath) {
        String filename = subPath.path().getFileName().toString();
        String ext = filename.substring(filename.lastIndexOf("."));

        for (String subsExtension : moveConfig.getSubsExt()) {
            if (ext.equals(subsExtension)) {
                return true;
            }
        }

        log.warn("Excluded sub based on extension {}", filename);
        return false;
    }
}
