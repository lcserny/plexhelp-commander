package net.cserny.move;

import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalFileService.WalkOptions;
import net.cserny.filesystem.LocalPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SubtitleMover {

    public static final String SUBS_SUBFOLDER = "Subs";

    private final Pattern episodeSegmentRegex = Pattern.compile(".*[eE](\\d{1,2}).*");

    @Autowired
    LocalFileService fileService;

    @Autowired
    FilesystemConfig filesystemConfig;

    @Autowired
    MoveConfig moveConfig;

    public List<MediaMoveError> moveSubs(SubsMoveOperation operation) {
        if (operation.subsSrc().path().toString().equals(filesystemConfig.getDownloadsPath())) {
            log.info("Path to move subs is root Downloads path, skipping operation...");
            return Collections.emptyList();
        }

        List<MediaMoveError> errors = new ArrayList<>();

        List<Path> subs;
        try {
            subs = fileService.walk(operation.subsSrc(), moveConfig.getSubsMaxDepth(), WalkOptions.ONLY_FILES)
                    .stream()
                    .filter(this::filterBySubExtension)
                    .toList();
        } catch (IOException e) {
            log.warn("Could not walk subs path", e);
            errors.add(new MediaMoveError(operation.subsSrc().path().toString(), e.getMessage()));
            return errors;
        }

        if (subs.isEmpty()) {
            log.info("No subs found for media " + operation.subsSrc().path());
            return Collections.emptyList();
        }

        errors.addAll(switch (operation.type()) {
            case MOVIE -> moveMovieSubs(operation, subs);
            case TV -> moveTvSubs(operation, subs);
        });

        return errors;
    }

    private List<MediaMoveError> moveTvSubs(SubsMoveOperation operation, List<Path> subs) {
        List<MediaMoveError> errors = new ArrayList<>();

        for (Path sub : subs) {
            String subName = sub.getFileName().toString();
            for (Path segment : sub) {
                String segmentStr = segment.toString();
                if (segmentStr.matches(episodeSegmentRegex.pattern())) {
                    subName = segmentStr + "." + subName;
                    break;
                }
            }

            LocalPath subSrc = fileService.toLocalPath(sub.toString());
            LocalPath subDest = fileService.toLocalPath(operation.subsDest().path().toString(), SUBS_SUBFOLDER, subName);

            try {
                fileService.move(subSrc, subDest);
            } catch (IOException e) {
                log.warn("Could not move sub", e);
                errors.add(new MediaMoveError(subSrc.path().toString(), e.getMessage()));
            }
        }

        return errors;
    }

    private List<MediaMoveError> moveMovieSubs(SubsMoveOperation operation, List<Path> subs) {
        List<MediaMoveError> errors = new ArrayList<>();

        for (Path sub : subs) {
            LocalPath subSrc = fileService.toLocalPath(sub.toString());
            String subFilename = sub.getFileName().toString();
            LocalPath subDest = fileService.toLocalPath(operation.subsDest().path().toString(), subFilename);

            try {
                fileService.move(subSrc, subDest);
            } catch (IOException e) {
                log.warn("Could not move sub", e);
                errors.add(new MediaMoveError(subSrc.path().toString(), e.getMessage()));
            }
        }

        return errors;
    }

    private boolean filterBySubExtension(Path subPath) {
        String filename = subPath.getFileName().toString();
        String ext = filename.substring(filename.lastIndexOf("."));

        for (String subsExtension : moveConfig.getSubsExt()) {
            if (ext.equals(subsExtension)) {
                return true;
            }
        }

        return false;
    }
}
