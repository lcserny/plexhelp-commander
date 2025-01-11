package net.cserny.qtorrent;

import lombok.extern.slf4j.Slf4j;
import net.cserny.VirtualExecutor;
import net.cserny.download.DownloadedMediaRepository;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.magnet.MagnetRepository;
import net.cserny.search.MediaIdentificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class TorrentsService {

    private final FilesystemProperties filesystemProperties;
    private final DownloadedMediaRepository downloadedMediaRepository;
    private final MagnetRepository magnetRepository;
    private final TorrentRestClient restClient;
    private final LocalFileService localFileService;
    private final MediaIdentificationService mediaIdentificationService;
    private final VirtualExecutor threadpool;

    @Autowired
    public TorrentsService(FilesystemProperties filesystemProperties,
                           DownloadedMediaRepository downloadedMediaRepository,
                           MagnetRepository magnetRepository,
                           TorrentRestClient restClient,
                           LocalFileService localFileService,
                           MediaIdentificationService mediaIdentificationService,
                           VirtualExecutor threadpool) {
        this.filesystemProperties = filesystemProperties;
        this.downloadedMediaRepository = downloadedMediaRepository;
        this.magnetRepository = magnetRepository;
        this.restClient = restClient;
        this.localFileService = localFileService;
        this.mediaIdentificationService = mediaIdentificationService;
        this.threadpool = threadpool;
    }

    public void addTorrent(String hash) {
        String sid = this.restClient.generateSid();

        List<TorrentFile> torrentFiles = this.restClient.listTorrents(sid, hash);
        log.info("Received {} torrent files from client", torrentFiles.size());

        List<TorrentFile> mediaTorrentFiles = processMediaTorrents(torrentFiles);

        var count = this.downloadedMediaRepository.upsertTorrents(mediaTorrentFiles, false);
        log.info("Added {} torrent files to downloads cache", count);
    }

    public void downloadTorrent(String hash) {
        String sid = this.restClient.generateSid();

        List<TorrentFile> torrentFiles = this.restClient.listTorrents(sid, hash);
        log.info("Received {} torrent files from client", torrentFiles.size());

        List<TorrentFile> mediaTorrentFiles = processMediaTorrents(torrentFiles);

        this.threadpool.executeWithCurrentSpan(Stream.of(
                () -> {
                    var count = this.downloadedMediaRepository.upsertTorrents(mediaTorrentFiles, true);
                    log.info("Updated {} torrent files to 'downloaded' in download cache", count);
                    return null;
                },
                () -> {
                    this.restClient.deleteTorrent(sid, hash, false);
                    log.info("Removed torrent from torrent client");
                    return null;
                },
                () -> {
                    this.magnetRepository.findByHashAndUpdateDownloaded(hash);
                    log.info("Updated magnet with hash {} to downloaded", hash);
                    return null;
                }
        ));
    }

    private List<TorrentFile> processMediaTorrents(List<TorrentFile> torrentFiles) {
        return torrentFiles.stream().map(torrentFile -> {
            LocalPath path = this.localFileService.toLocalPath(filesystemProperties.getDownloadsPath(), torrentFile.name());
            boolean isMedia = mediaIdentificationService.isMedia(path);
            return new TorrentFile(torrentFile.name(), torrentFile.size(), isMedia);
        }).toList();
    }
}
