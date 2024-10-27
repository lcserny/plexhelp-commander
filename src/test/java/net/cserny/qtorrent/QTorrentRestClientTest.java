package net.cserny.qtorrent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QTorrentRestClientTest {

    private static final String BASE_URL = "someUrl";
    private static final String USER = "myUser";
    private static final String PASS = "myPass";

    private RestTemplate restTemplateMock;
    private TorrentProperties properties;

    private QTorrentRestClient restClient;

    @BeforeEach
    public void setup() {
        this.restTemplateMock = mock(RestTemplate.class);
        this.properties = new TorrentProperties();
        this.properties.setBaseUrl(BASE_URL);
        this.properties.setUsername(USER);
        this.properties.setPassword(PASS);

        this.restClient = new QTorrentRestClient(this.restTemplateMock, this.properties);
    }

    @Test
    @DisplayName("Generating SID from response cookie")
    public void generateSid() {
        var sid = "something";
        ResponseEntity<String> response = ResponseEntity.ok().header("Set-Cookie", "SID=" + sid + ";").build();
        when(this.restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        String returnedSid = this.restClient.generateSid();

        assertEquals(sid, returnedSid);
    }

    @Test
    @DisplayName("Adding magnet does not throw any exception")
    public void addMagnet() {
        var sid = "irrelevant";
        var magnet = "some magnet link";

        ResponseEntity<String> response = ResponseEntity.ok().build();
        when(this.restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        this.restClient.addMagnet(sid, magnet);
    }
}