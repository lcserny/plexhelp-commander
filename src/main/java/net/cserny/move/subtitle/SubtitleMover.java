package net.cserny.move.subtitle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.generated.MediaMoveError;
import net.cserny.move.MediaGrouper;
import net.cserny.move.MediaGrouper.LangKey;
import net.cserny.move.MediaInfoExtractor;
import net.cserny.move.MediaInfoExtractor.IndexedLangData;
import net.cserny.move.MediaInfoExtractor.MediaInfo;
import net.cserny.move.MoveProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static net.cserny.support.UtilityProvider.toLoggableString;
import static net.cserny.filesystem.ExcludingFileVisitor.WalkOptions.ONLY_FILES;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubtitleMover {

    private final LocalFileService fileService;
    private final FilesystemProperties filesystemConfig;
    private final MoveProperties moveConfig;
    private final MediaGrouper mediaGrouper;

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

        log.info("{} type subs found {}", operation.type().toString(), toLoggableString(subs));

        errors.addAll(moveSubsInternal(operation, subs));

        return errors;
    }

    private List<MediaMoveError> moveSubsInternal(SubsMoveOperation operation, List<LocalPath> subs) {
        List<MediaMoveError> errors = new ArrayList<>();

        for (Map.Entry<LangKey, List<LocalPath>> entry : mediaGrouper.subsByLang(subs).entrySet()) {
            List<LocalPath> subsForLang = entry.getValue();
            int nrOfSubsForLang = subsForLang.size();

            for (int i = 0; i < subsForLang.size(); i++) {
                LocalPath sub = subsForLang.get(i);
                LocalPath subSrc = fileService.toLocalPath(sub.toString());
                String subNameOnly = sub.path().getFileName().toString();
                IndexedLangData langData = IndexedLangData.builder().lang(entry.getKey()).singleMediaForLang(nrOfSubsForLang == 1).indexNr(i + 1).build();

                switch (moveOneSub(operation, subSrc, subNameOnly, langData)) {
                    case SubtitleMoveResult.Failure f -> errors.add(f.error());
                    case SubtitleMoveResult.Success s -> {}
                }
            }
        }

        return errors;
    }

    private SubtitleMoveResult moveOneSub(SubsMoveOperation operation, LocalPath subSrc, String subNameOnly, IndexedLangData langData) {
        MediaInfoExtractor extractor = new MediaInfoExtractor(operation.group().getName(), operation.group().getSeason(), operation.type(), subNameOnly, langData);
        MediaInfo mediaInfo = extractor.extractMediaInfo();
        LocalPath subDest = fileService.toLocalPath(operation.destRoot(), mediaInfo.destinationPathSegments());

        try {
            log.info("Moving sub {} to {}", subSrc, subDest);
            fileService.move(subSrc, subDest);
            return new SubtitleMoveResult.Success();
        } catch (IOException e) {
            log.warn("Could not move sub: {}", e.getMessage());
            return new SubtitleMoveResult.Failure(new MediaMoveError().mediaPath(subSrc.path().toString()).error(e.getMessage()));
        }
    }

    private boolean filterBySubExtension(LocalPath subPath) {
        String filename = subPath.path().getFileName().toString();
        String ext = filename.substring(filename.lastIndexOf("."));

        for (String subsExtension : moveConfig.getSubsExt()) {
            if (ext.equals(subsExtension)) {
                return true;
            }
        }

        log.debug("Excluded sub based on extension {}", filename);
        return false;
    }
}
