package net.cserny.core.move.automove;

import lombok.RequiredArgsConstructor;
import net.cserny.api.*;
import net.cserny.api.dto.LocalPath;
import net.cserny.api.dto.MediaInfo;
import net.cserny.generated.*;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AutomoveSupportAggregator
        implements DownloadedMediaManipulator, NameNormalizer, MediaFileGroupGenerator, NameOptionsProducer, MediaMover {

    private final DownloadedMediaManipulator mediaDownloadService;
    private final NameNormalizer defaultNameNormalizer;
    private final MediaFileGroupGenerator mediaSearchService;
    private final NameOptionsProducer mediaRenameService;
    private final MediaMover mediaMoveService;

    @Override
    public RenamedMediaOptions produceNames(String name, MediaFileType type) {
        return mediaRenameService.produceNames(name, type);
    }

    @Override
    public List<DownloadedMediaData> findForAutoMove(int limit) {
        return mediaDownloadService.findForAutoMove(limit);
    }

    @Override
    public void saveAll(List<DownloadedMediaData> medias) {
        mediaDownloadService.saveAll(medias);
    }

    @Override
    public List<MediaFileGroup> generateMediaFileGroupsFromDownloads(List<String> relativeMediaPaths) {
        return mediaSearchService.generateMediaFileGroupsFromDownloads(relativeMediaPaths);
    }

    @Override
    public List<MediaMoveError> moveMedia(MediaFileGroup fileGroup, MediaFileType type, MediaDescriptionData mediaDesc) {
        return mediaMoveService.moveMedia(fileGroup, type, mediaDesc);
    }

    @Override
    public void persistMovedMedia(LocalPath srcPath, LocalPath destPath, MediaInfo mediaInfo, MediaDescriptionData mediaDesc) {
        mediaMoveService.persistMovedMedia(srcPath, destPath, mediaInfo, mediaDesc);
    }

    @Override
    public NameYear normalize(String name) {
        return defaultNameNormalizer.normalize(name);
    }
}
