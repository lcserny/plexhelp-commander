package net.cserny.qtorrent;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.cserny.download.internal.DownloadedMediaRepository;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.magnet.MagnetRepository;
import net.cserny.search.MediaIdentificationService;
import net.cserny.support.UtilityProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
@Slf4j
@Service
public class TorrentsService {

    private final FilesystemProperties filesystemProperties;
    private final DownloadedMediaRepository downloadedMediaRepository;
    private final MagnetRepository magnetRepository;
    private final TorrentRestClient restClient;
    private final LocalFileService localFileService;
    private final MediaIdentificationService mediaIdentificationService;
    private final ExecutorService executorService;

    public void addTorrent(String hash) {
        String sid = this.restClient.generateSid();

        List<TorrentFile> torrentFiles = this.restClient.listTorrents(sid, hash);
        log.info("Received {} torrent files from client", torrentFiles.size());

        List<TorrentFile> mediaTorrentFiles = processMediaTorrents(torrentFiles);
        log.info("{} torrent files are media files", mediaTorrentFiles.stream().filter(TorrentFile::isMedia).count());

        var count = this.downloadedMediaRepository.upsertTorrents(mediaTorrentFiles, false);
        log.info("Added {} torrent files to downloads cache", count);
    }

    @SneakyThrows
    public void downloadTorrent(String hash) {
        String sid = this.restClient.generateSid();

        List<TorrentFile> torrentFiles = this.restClient.listTorrents(sid, hash);
        log.info("Received {} torrent files from client", torrentFiles.size());

        List<TorrentFile> mediaTorrentFiles = processMediaTorrents(torrentFiles);
        log.info("{} torrent files are media files", mediaTorrentFiles.stream().filter(TorrentFile::isMedia).count());

        // needs improvements: what happens if torrentDeleted but upsertTorrents failed? and similar
        Callable<Void> upsertTorrents = () -> {
            var count = this.downloadedMediaRepository.upsertTorrents(mediaTorrentFiles, true);
            log.info("Updated {} torrent files to 'downloaded' in download cache", count);
            return null;
        };

        Callable<Void> deleteTorrent = () -> {
            this.restClient.deleteTorrent(sid, hash, false);
            log.info("Removed torrent from torrent client");
            return null;
        };

        Callable<Void> updateDownloaded = () -> {
            this.magnetRepository.findByHashAndUpdateDownloaded(hash);
            log.info("Updated magnet with hash {} to downloaded", hash);
            return null;
        };

        this.executorService.invokeAll(List.of(upsertTorrents, deleteTorrent, updateDownloaded)).forEach(UtilityProvider::getUncheckedThrowing);
    }

    private List<TorrentFile> processMediaTorrents(List<TorrentFile> torrentFiles) {
        return torrentFiles.stream().map(torrentFile -> {
            LocalPath path = this.localFileService.toLocalPath(filesystemProperties.getDownloadsPath(), torrentFile.name());
            boolean isMedia = mediaIdentificationService.isMedia(path);
            return new TorrentFile(torrentFile.name(), torrentFile.size(), isMedia);
        }).toList();
    }
}
