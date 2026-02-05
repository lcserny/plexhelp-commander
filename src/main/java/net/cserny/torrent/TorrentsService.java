package net.cserny.torrent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.search.MediaIdentificationService;
import net.cserny.support.events.CommanderEventPublisher;
import net.cserny.support.events.Events.TorrentDownloadCompleted;
import net.cserny.support.events.Events.TorrentDownloadStarted;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiConsumer;

@SuppressWarnings("LoggingSimilarMessage")
@RequiredArgsConstructor
@Slf4j
@Service
public class TorrentsService {

    private final FilesystemProperties filesystemProperties;
    private final CommanderEventPublisher eventPublisher;
    private final TorrentRestClient restClient;
    private final LocalFileService localFileService;
    private final MediaIdentificationService mediaIdentificationService;

    public void markTorrentDownloadStarted(String hash) {
        processWithSid(hash, (sid, torrentFiles) -> {
            eventPublisher.sendSync(TorrentDownloadStarted.builder()
                    .hash(hash)
                    .torrentFiles(torrentFiles)
                    .build());
        });
    }

    public void markTorrentDownloadCompleted(String hash) {
        processWithSid(hash, (sid, torrentFiles) -> {
            eventPublisher.sendSync(TorrentDownloadCompleted.builder()
                    .hash(hash)
                    .torrentFiles(torrentFiles)
                    .build());

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
            LocalPath path = this.localFileService.toLocalPath(filesystemProperties.getDownloadsPath(), torrentFile.name());
            boolean isMedia = mediaIdentificationService.isMedia(path);
            return new TorrentFile(torrentFile.name(), torrentFile.size(), isMedia);
        }).toList();
    }
}
