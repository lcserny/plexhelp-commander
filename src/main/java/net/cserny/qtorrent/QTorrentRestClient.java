package net.cserny.qtorrent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class QTorrentRestClient implements TorrentRestClient {

    private final RestTemplate restTemplate;

    private final String loginUrl;
    private final String addUrl;
    private final String username;
    private final String password;

    @Autowired
    public QTorrentRestClient(RestTemplate restTemplate, TorrentProperties properties) {
        this.restTemplate = restTemplate;
        this.username = properties.getUsername();
        this.password = properties.getPassword();
        this.loginUrl = properties.getBaseUrl() + "/api/v2/auth/login";
        this.addUrl = properties.getBaseUrl() + "/api/v2/torrents/add";
    }

    @Override
    public String generateSid() {
        var headers = this.createFormHeaders();

        var formParams = new LinkedMultiValueMap<>();
        formParams.add("username", this.username);
        formParams.add("password", this.password);

        var request = new HttpEntity<>(formParams, headers);

        var response = restTemplate.exchange(this.loginUrl, HttpMethod.POST, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Could not login to torrent client, error code: " + response.getStatusCode());
        }

        var cookies = response.getHeaders().get("Set-Cookie");
        if (cookies == null || cookies.isEmpty() || !cookies.getFirst().contains("SID")) {
            throw new RestClientException("No SID found in response cookies");
        }

        var sid = cookies.getFirst().substring(4, cookies.getFirst().indexOf(";"));
        log.info("SID generated: {}", sid);

        return sid;
    }

    @Override
    public void addMagnet(String sid, String magnetUrl) {
        var headers = this.createFormHeaders();
        this.addSIDCookie(headers, sid);

        var formParams = new LinkedMultiValueMap<>();
        formParams.add("urls", magnetUrl);

        var request = new HttpEntity<>(formParams, headers);

        var response = restTemplate.exchange(this.addUrl, HttpMethod.POST, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Could not add magnet to torrent client, error code: " + response.getStatusCode());
        }
    }

    private HttpHeaders createFormHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private void addSIDCookie(HttpHeaders headers, String sid) {
        headers.set("Cookie", "SID=" + sid);
    }
}
