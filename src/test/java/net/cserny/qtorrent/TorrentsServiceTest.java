package net.cserny.qtorrent;

import net.cserny.MongoTestConfiguration;
import net.cserny.TestConfig;
import net.cserny.VirtualExecutor;
import net.cserny.download.DownloadedMedia;
import net.cserny.download.DownloadedMediaRepository;
import net.cserny.filesystem.AbstractInMemoryFileService;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.filesystem.LocalPath;
import net.cserny.magnet.Magnet;
import net.cserny.magnet.MagnetRepository;
import net.cserny.search.MediaIdentificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        TestConfig.class,
        VirtualExecutor.class
})
@DataMongoTest
@Testcontainers
class TorrentsServiceTest extends AbstractInMemoryFileService {

    @Autowired
    private TorrentsService service;

    @Autowired
    private MagnetRepository magnetRepository;

    @Autowired
    private DownloadedMediaRepository mediaRepository;

    @Autowired
    FilesystemProperties filesystemConfig;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private MediaIdentificationService identificationService;

    @BeforeEach
    void setUp() throws IOException {
        createDirectories(filesystemConfig.getDownloadsPath());
        createDirectories(filesystemConfig.getMoviesPath());
        createDirectories(filesystemConfig.getTvPath());
    }

    @Test
    @DisplayName("adding new torrent, adds them the download cache")
    public void addingTorrentSavesToRepository() {
        setupSIDMock();

        var torrent1Name = "torrent1";
        var torrents = List.of(new TorrentFile(torrent1Name, 6, true));
        ResponseEntity<List<TorrentFile>> response = ResponseEntity.ok().body(torrents);
        when(this.restTemplate.exchange(contains("files"), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(response);
        when(identificationService.isMedia(any(LocalPath.class))).thenReturn(true);

        service.addTorrent("someHash");

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

        setupSIDMock();

        var torrents = List.of(new TorrentFile(torrentName, 6, true));
        ResponseEntity<List<TorrentFile>> response = ResponseEntity.ok().body(torrents);
        when(this.restTemplate.exchange(contains("files"), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(response);
        ResponseEntity<String> delResponse = ResponseEntity.ok().build();
        when(this.restTemplate.exchange(contains("delete"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class))).thenReturn(delResponse);
        when(identificationService.isMedia(any(LocalPath.class))).thenReturn(true);

        service.downloadTorrent("someHash2");

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

        setupSIDMock();

        var torrents = List.of(new TorrentFile(torrentName, 6, true));
        ResponseEntity<List<TorrentFile>> response = ResponseEntity.ok().body(torrents);
        when(this.restTemplate.exchange(contains("files"), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(response);
        ResponseEntity<String> delResponse = ResponseEntity.ok().build();
        when(this.restTemplate.exchange(contains("delete"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class))).thenReturn(delResponse);
        when(identificationService.isMedia(any(LocalPath.class))).thenReturn(true);

        service.downloadTorrent("someHash2");

        verify(this.restTemplate, times(1)).exchange(contains("delete"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
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

        setupSIDMock();

        ResponseEntity<List<TorrentFile>> response = ResponseEntity.ok().body(List.of());
        when(this.restTemplate.exchange(contains("files"), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(response);
        ResponseEntity<String> delResponse = ResponseEntity.ok().build();
        when(this.restTemplate.exchange(contains("delete"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class))).thenReturn(delResponse);
        when(identificationService.isMedia(any(LocalPath.class))).thenReturn(true);

        service.downloadTorrent(hash);

        Magnet results = magnetRepository.findByHash(hash);
        assertNotNull(results);
        assertTrue(results.isDownloaded());
    }

    private void setupSIDMock() {
        var sid = "something";
        ResponseEntity<String> response = ResponseEntity.ok().header("Set-Cookie", "SID=" + sid + ";").build();
        when(this.restTemplate.exchange(contains("login"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);
    }
}