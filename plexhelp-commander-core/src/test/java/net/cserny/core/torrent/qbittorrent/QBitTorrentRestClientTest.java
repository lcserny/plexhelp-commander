package net.cserny.core.torrent.qbittorrent;

import net.cserny.api.QBitTorrentRestApi;
import net.cserny.config.TorrentProperties;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.http.*;

import static org.mockito.Mockito.mock;

class QBitTorrentRestClientTest {

    private static final String BASE_URL = "someUrl";
    private static final String USER = "myUser";
    private static final String PASS = "myPass";

    private QBitTorrentRestClient restClient;

    @BeforeEach
    public void setup() {
        QBitTorrentRestApi restApi = mock(QBitTorrentRestApi.class);
        TorrentProperties props = new TorrentProperties();
        props.setBaseUrl(BASE_URL);
        props.setUsername(USER);
        props.setPassword(PASS);

        this.restClient = new QBitTorrentRestClient(restApi, props);
    }

    // TODO add tests
}