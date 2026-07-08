package net.cserny.core.torrent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.*;
import net.cserny.api.dto.TorrentFile;
import net.cserny.config.FilesystemProperties;
import net.cserny.api.dto.LocalPath;
import net.cserny.core.torrent.qbittorrent.QBitTorrentRestApi;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("LoggingSimilarMessage")
@RequiredArgsConstructor
@Slf4j
@Service
public class TorrentsService {

    private final FilesystemProperties filesystemProperties;
    private final DownloadedTorrentProcessor torrentProcessor;
    private final MagnetUpdater magnetUpdater;
    private final QBitTorrentRestApi restClient;
    private final LocalPathHandler localPathHandler;
    private final MediaIdentifier mediaIdentifier;

    public void markTorrentDownloadStarted(String hash) {
        processTorrent(hash, torrentProcessor::addTorrents);
    }

    public void markTorrentDownloadCompleted(String hash) {
        processTorrent(hash, (torrentFiles) -> {
            torrentProcessor.updateDownloaded(torrentFiles);
            magnetUpdater.markMagnetsDownloaded(hash);
            this.restClient.torrentDelete(hash, false);
            log.info("Removed torrent from torrent client");
        });
    }

    private void processTorrent(String hash, Consumer<List<TorrentFile>> consumer) {
        List<TorrentFile> torrentFiles = this.restClient.torrentFiles(hash);
        log.info("Received {} torrent files from client", torrentFiles.size());

        List<TorrentFile> mediaTorrentFiles = enrichMediaTorrents(torrentFiles);
        log.info("{} torrent files are media files", mediaTorrentFiles.stream().filter(TorrentFile::isMedia).count());

        consumer.accept(mediaTorrentFiles);
    }

    private List<TorrentFile> enrichMediaTorrents(List<TorrentFile> torrentFiles) {
        return torrentFiles.stream().map(torrentFile -> {
            LocalPath path = localPathHandler.toLocalPath(filesystemProperties.getDownloadsPath(), torrentFile.name());
            boolean isMedia = mediaIdentifier.isMedia(path);
            return new TorrentFile(torrentFile.name(), torrentFile.size(), isMedia);
        }).toList();
    }
}
