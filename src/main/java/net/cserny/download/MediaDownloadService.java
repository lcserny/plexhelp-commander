package net.cserny.download;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.support.DataMapper;
import net.cserny.download.internal.DownloadedMediaRepository;
import net.cserny.generated.DownloadedMediaData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MediaDownloadService {

    private final DownloadedMediaRepository repository;

    public List<DownloadedMediaData> retrieveAllFrom(LocalDate date, List<String> names, Boolean downloaded) {
        List<DownloadedMedia> media = repository.findAllWith(date, downloaded, names);
        return media.stream().map(DataMapper.INSTANCE::downloadedMediaToDownloadedMediaData).toList();
    }

    public Page<DownloadedMediaData> retrieveAllPaginatedFrom(LocalDate date, List<String> names, Boolean downloaded, Pageable pageable) {
        Page<DownloadedMedia> mediaPage = repository.findAllPaginatedWith(date, downloaded, names, pageable);
        return mediaPage.map(DataMapper.INSTANCE::downloadedMediaToDownloadedMediaData);
    }

    public void addTorrents(List<TorrentFile> torrentFiles) {
        var count = repository.upsertTorrents(torrentFiles, false);
        log.info("Added {} torrent files to downloads cache", count);
    }

    public void updateDownloaded(List<TorrentFile> torrentFiles) {
        var count = repository.upsertTorrents(torrentFiles, true);
        log.info("Updated {} torrent files to downloads cache", count);
    }
}
