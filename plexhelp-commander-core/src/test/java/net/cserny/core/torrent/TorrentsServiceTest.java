package net.cserny.core.torrent;

import net.cserny.IntegrationTest;
import net.cserny.api.MediaIdentifier;
import net.cserny.api.dto.TorrentFile;
import net.cserny.core.download.DownloadedMedia;
import net.cserny.core.download.internal.DownloadedMediaRepository;
import net.cserny.core.magnet.Magnet;
import net.cserny.core.magnet.MagnetRepository;
import net.cserny.config.FilesystemProperties;
import net.cserny.api.dto.LocalPath;
import net.cserny.core.torrent.qbittorrent.QBitTorrentRestApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TorrentsServiceTest extends IntegrationTest {

    @Autowired
    private TorrentsService service;

    @Autowired
    private MagnetRepository magnetRepository;

    @Autowired
    private DownloadedMediaRepository mediaRepository;

    @Autowired
    FilesystemProperties filesystemConfig;

    @MockitoBean
    private QBitTorrentRestApi restClient;

    @MockitoBean
    private MediaIdentifier mediaIdentifier;

    @BeforeEach
    void setUp() throws IOException {
        createDirectories(filesystemConfig.getDownloadsPath());
        createDirectories(filesystemConfig.getMoviesPath());
        createDirectories(filesystemConfig.getTvPath());
    }

    @Test
    @DisplayName("adding new torrent, adds them the download cache")
    public void addingTorrentSavesToRepository() {
        var torrent1Name = "torrent1";
        var torrents = List.of(new TorrentFile(torrent1Name, 6, true));
        when(restClient.torrentFiles(anyString())).thenReturn(torrents);
        when(mediaIdentifier.isMedia(any(LocalPath.class))).thenReturn(true);

        service.markTorrentDownloadStarted("someHash");

        List<DownloadedMedia> results = mediaRepository.findAllWith(null, null, List.of(torrent1Name));
        assertEquals(1, results.size());
        assertTrue(results.getFirst().getFileName().contains(torrent1Name));
    }

    @Test
    @DisplayName("downloading new torrent, marks them as downloaded in download cache")
    public void downloadingTorrentUpdatesDownloadedMedia() {
        var torrentName = "torrent2";

        DownloadedMedia media1 = new DownloadedMedia();
        media1.setFileName(torrentName);
        media1.setFileSize(6);
        media1.setDateDownloaded(Instant.now(Clock.systemUTC()));
        media1.setTriedAutoMove(false);
        media1.setDownloadComplete(false);
        mediaRepository.save(media1);

        var torrents = List.of(new TorrentFile(torrentName, 6, true));
        when(restClient.torrentFiles(anyString())).thenReturn(torrents);
        doNothing().when(restClient).torrentDelete(anyString(), anyBoolean());
        when(mediaIdentifier.isMedia(any(LocalPath.class))).thenReturn(true);

        service.markTorrentDownloadCompleted("someHash2");

        List<DownloadedMedia> results = mediaRepository.findAllWith(null, null, List.of(torrentName));
        assertEquals(1, results.size());
        assertTrue(results.getFirst().isDownloadComplete());
    }

    @Test
    @DisplayName("downloading new torrent, removes them from client")
    public void downloadingTorrentRemovesThemFromClient() {
        var torrentName = "torrent3";

        DownloadedMedia media1 = new DownloadedMedia();
        media1.setFileName(torrentName);
        media1.setFileSize(6);
        media1.setDateDownloaded(Instant.now(Clock.systemUTC()));
        media1.setTriedAutoMove(false);
        media1.setDownloadComplete(false);
        mediaRepository.save(media1);

        var torrents = List.of(new TorrentFile(torrentName, 6, true));
        when(restClient.torrentFiles(anyString())).thenReturn(torrents);
        doNothing().when(restClient).torrentDelete(anyString(), anyBoolean());
        when(mediaIdentifier.isMedia(any(LocalPath.class))).thenReturn(true);

        service.markTorrentDownloadCompleted("someHash2");

        verify(restClient, times(1)).torrentDelete(anyString(), anyBoolean());
    }

    @Test
    @DisplayName("downloading new torrent, updates magnet found to downloaded")
    public void downloadingTorrentUpdatesMagnet() {
        var hash = "hashimodo";

        Magnet magnet = new Magnet();
        magnet.setHash(hash);
        magnet.setName("aaa");
        magnet.setDownloaded(false);
        magnetRepository.save(magnet);

        when(restClient.torrentFiles(anyString())).thenReturn(List.of());
        doNothing().when(restClient).torrentDelete(anyString(), anyBoolean());
        when(mediaIdentifier.isMedia(any(LocalPath.class))).thenReturn(true);

        service.markTorrentDownloadCompleted(hash);

        Magnet results = magnetRepository.findByHash(hash);
        assertNotNull(results);
        assertTrue(results.isDownloaded());
    }
}