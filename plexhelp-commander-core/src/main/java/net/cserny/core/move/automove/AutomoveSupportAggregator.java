package net.cserny.core.move.automove;

import lombok.RequiredArgsConstructor;
import net.cserny.api.*;
import net.cserny.generated.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("SpringQualifierCopyableLombok")
@RequiredArgsConstructor
@Component
public class AutomoveSupportAggregator implements AutomoveSupport {

    @Qualifier("mediaDownloadService")
    private final DownloadedMediaManipulator downloadedManipulator;
    @Qualifier("defaultNameNormalizer")
    private final NameNormalizer normalizer;
    @Qualifier("mediaSearchService")
    private final MediaFileGroupGenerator fileGroupGenerator;
    @Qualifier("mediaRenameService")
    private final NameOptionsProducer nameOptionsProducer;
    @Qualifier("mediaMoveService")
    private final MediaMover mediaMover;

    @Override
    public RenamedMediaOptions produceNames(String name, MediaFileType type) {
        return nameOptionsProducer.produceNames(name, type);
    }

    @Override
    public List<DownloadedMediaData> findForAutoMove(int limit) {
        return downloadedManipulator.findForAutoMove(limit);
    }

    @Override
    public void saveAll(List<DownloadedMediaData> medias) {
        downloadedManipulator.saveAll(medias);
    }

    @Override
    public List<MediaFileGroup> generateMediaFileGroupsFromDownloads(List<String> relativeMediaPaths) {
        return fileGroupGenerator.generateMediaFileGroupsFromDownloads(relativeMediaPaths);
    }

    @Override
    public List<MediaMoveError> moveMedia(MediaFileGroup fileGroup, MediaFileType type, MediaDescriptionData mediaDesc) {
        return mediaMover.moveMedia(fileGroup, type, mediaDesc);
    }

    @Override
    public NameYear normalize(String name) {
        return normalizer.normalize(name);
    }
}
