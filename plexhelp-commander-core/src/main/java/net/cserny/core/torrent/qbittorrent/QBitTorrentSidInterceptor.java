package net.cserny.core.torrent.qbittorrent;

import lombok.extern.slf4j.Slf4j;
import net.cserny.config.TorrentProperties;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.List;

import static net.cserny.core.torrent.qbittorrent.QBitTorrentRestApi.Routes.AUTH_LOGIN_URI;
import static net.cserny.core.torrent.qbittorrent.QBitTorrentRestApi.Routes.ROOT_URI;
import static org.springframework.http.HttpHeaders.COOKIE;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

@Slf4j
public class QBitTorrentSidInterceptor implements ClientHttpRequestInterceptor {

    private final MultiValueMap<String, String> authParams;
    private final RestClient restClient;

    public QBitTorrentSidInterceptor(TorrentProperties torrentProperties, RestClient restClient) {
        this.authParams = new LinkedMultiValueMap<>();
        this.authParams.add("username", torrentProperties.getUsername());
        this.authParams.add("password", torrentProperties.getPassword());

        this.restClient = restClient;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ResponseEntity<Void> response = this.restClient.post()
                .uri(ROOT_URI + AUTH_LOGIN_URI)
                .body(this.authParams)
                .retrieve()
                .toBodilessEntity();

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Could not login to torrent client, error code: " + response.getStatusCode());
        }

        List<String> cookies = response.getHeaders().get(SET_COOKIE);
        if (cookies == null || cookies.isEmpty() || !cookies.getFirst().contains("SID")) {
            throw new RestClientException("No SID found in response cookies");
        }

        var fullSid = cookies.getFirst().substring(0, cookies.getFirst().indexOf(";")).trim();
        log.info("SID generated: {}", fullSid);

        String[] sidParts = fullSid.split("=");
        request.getHeaders().set(COOKIE, sidParts[0] + "=" + sidParts[1]);

        return execution.execute(request, body);
    }
}
