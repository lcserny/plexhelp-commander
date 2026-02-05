package net.cserny.core.torrent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.DownloadedTorrentProcessor;
import net.cserny.api.LocalPathConverter;
import net.cserny.api.MagnetUpdater;
import net.cserny.api.MediaIdentifier;
import net.cserny.api.dto.TorrentFile;
import net.cserny.fs.FilesystemProperties;
import net.cserny.fs.LocalPath;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiConsumer;

@SuppressWarnings("LoggingSimilarMessage")
@RequiredArgsConstructor
@Slf4j
@Service
class TorrentsService {

    private final FilesystemProperties filesystemProperties;
    private final DownloadedTorrentProcessor torrentProcessor;
    private final MagnetUpdater magnetUpdater;
    private final TorrentRestClient restClient;
    private final LocalPathConverter localPathConverter;
    private final MediaIdentifier mediaIdentifier;

    public void markTorrentDownloadStarted(String hash) {
        processWithSid(hash, (sid, torrentFiles) -> {
            torrentProcessor.addTorrents(torrentFiles);
        });
    }

    public void markTorrentDownloadCompleted(String hash) {
        processWithSid(hash, (sid, torrentFiles) -> {
            torrentProcessor.updateDownloaded(torrentFiles);

            magnetUpdater.markMagnetsDownloaded(hash);

            this.restClient.deleteTorrent(sid, hash, false);
            log.info("Removed torrent from torrent client");
        });
    }

    private void processWithSid(String hash, BiConsumer<String, List<TorrentFile>> consumer) {
        String sid = this.restClient.generateSid();

        List<TorrentFile> torrentFiles = this.restClient.listTorrents(sid, hash);
        log.info("Received {} torrent files from client", torrentFiles.size());

        List<TorrentFile> mediaTorrentFiles = enrichMediaTorrents(torrentFiles);
        log.info("{} torrent files are media files", mediaTorrentFiles.stream().filter(TorrentFile::isMedia).count());

        consumer.accept(sid, mediaTorrentFiles);
    }

    private List<TorrentFile> enrichMediaTorrents(List<TorrentFile> torrentFiles) {
        return torrentFiles.stream().map(torrentFile -> {
            LocalPath path = localPathConverter.toLocalPath(filesystemProperties.getDownloadsPath(), torrentFile.name());
            boolean isMedia = mediaIdentifier.isMedia(path);
            return new TorrentFile(torrentFile.name(), torrentFile.size(), isMedia);
        }).toList();
    }
}
