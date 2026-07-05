package net.cserny.core.torrent.qbittorrent;

import net.cserny.api.dto.TorrentFile;
import net.cserny.config.TorrentProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QBitTorrentRestClientTest {

    private static final String BASE_URL = "someUrl";
    private static final String USER = "myUser";
    private static final String PASS = "myPass";

    private QBitTorrentRestClient restClient;
    private QBitTorrentRestApi restApi;

    @BeforeEach
    public void setup() {
        this.restApi = mock(QBitTorrentRestApi.class);
        TorrentProperties props = new TorrentProperties();
        props.setBaseUrl(BASE_URL);
        props.setUsername(USER);
        props.setPassword(PASS);

        this.restClient = new QBitTorrentRestClient(restApi, props);

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.add(HttpHeaders.SET_COOKIE, "SID=mySidValue; Path=/; HttpOnly");
        ResponseEntity<Void> authResponse = new ResponseEntity<>(authHeaders, HttpStatus.OK);
        when(restApi.authLogin(any(HttpHeaders.class), any(MultiValueMap.class))).thenReturn(authResponse);
    }

    @Test
    void addMagnet_Success() {
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.OK);
        when(restApi.torrentAdd(any(HttpHeaders.class), any(MultiValueMap.class))).thenReturn(response);

        assertDoesNotThrow(() -> restClient.addMagnet("http://magnet-url"));
        verify(restApi).torrentAdd(any(HttpHeaders.class), any(MultiValueMap.class));
    }

    @Test
    void listTorrents_Success() {
        List<TorrentFile> expectedFiles = List.of(new TorrentFile("test.file", 100L, true));
        ResponseEntity<List<TorrentFile>> response = new ResponseEntity<>(expectedFiles, HttpStatus.OK);
        when(restApi.torrentFiles(any(HttpHeaders.class), any(MultiValueMap.class))).thenReturn(response);

        List<TorrentFile> files = restClient.listTorrents("hash");

        assertNotNull(files);
        assertEquals(1, files.size());
        verify(restApi).torrentFiles(any(HttpHeaders.class), any(MultiValueMap.class));
    }

    @Test
    void deleteTorrent_Success() {
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.OK);
        when(restApi.torrentDelete(any(HttpHeaders.class), any(MultiValueMap.class))).thenReturn(response);

        assertDoesNotThrow(() -> restClient.deleteTorrent("hash", true));
        verify(restApi).torrentDelete(any(HttpHeaders.class), any(MultiValueMap.class));
    }
}
