package net.cserny.core.download;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.AutomoveDownloadedManipulator;
import net.cserny.api.DownloadedTorrentProcessor;
import net.cserny.support.DataMapper;
import net.cserny.core.download.internal.DownloadedMediaRepository;
import net.cserny.generated.DownloadedMediaData;
import net.cserny.api.dto.TorrentFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
class MediaDownloadService implements DownloadedTorrentProcessor, AutomoveDownloadedManipulator {

    private final DownloadedMediaRepository repository;

    public List<DownloadedMediaData> retrieveAllFrom(LocalDate date, List<String> names, Boolean downloaded) {
        List<DownloadedMedia> media = repository.findAllWith(date, downloaded, names);
        return media.stream().map(DataMapper.INSTANCE::downloadedMediaToDownloadedMediaData).toList();
    }

    public Page<DownloadedMediaData> retrieveAllPaginatedFrom(LocalDate date, List<String> names, Boolean downloaded, Pageable pageable) {
        Page<DownloadedMedia> mediaPage = repository.findAllPaginatedWith(date, downloaded, names, pageable);
        return mediaPage.map(DataMapper.INSTANCE::downloadedMediaToDownloadedMediaData);
    }

    @Override
    public void addTorrents(List<TorrentFile> torrentFiles) {
        var count = repository.upsertTorrents(torrentFiles, false);
        log.info("Added {} torrent files to downloads cache", count);
    }

    @Override
    public void updateDownloaded(List<TorrentFile> torrentFiles) {
        var count = repository.upsertTorrents(torrentFiles, true);
        log.info("Updated {} torrent files to downloads cache", count);
    }

    @Override
    public List<DownloadedMediaData> findForAutoMove(int limit) {
        return repository.findForAutoMove(limit).stream().map(DataMapper.INSTANCE::downloadedMediaToDownloadedMediaData).toList();
    }

    @Override
    public void saveAll(List<DownloadedMediaData> medias) {
        repository.saveAll(medias.stream().map(DataMapper.INSTANCE::downloadedMediaDataToDownloadedMedia).toList());
    }
}
