package net.cserny.qtorrent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class QTorrentRestClient implements TorrentRestClient {

    private final RestTemplate restTemplate;

    private final String loginUrl;
    private final String addUrl;
    private final String listUrl;
    private final String deleteUrl;
    private final String username;
    private final String password;

    @Autowired
    public QTorrentRestClient(RestTemplate restTemplate, TorrentProperties properties) {
        this.restTemplate = restTemplate;
        this.username = properties.getUsername();
        this.password = properties.getPassword();
        this.loginUrl = properties.getBaseUrl() + "/api/v2/auth/login";
        this.addUrl = properties.getBaseUrl() + "/api/v2/torrents/add";
        this.listUrl = properties.getBaseUrl() + "/api/v2/torrents/files";
        this.deleteUrl = properties.getBaseUrl() + "/api/v2/torrents/delete";
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

    @Override
    public List<TorrentFile> listTorrents(String sid, String hash) {
        var headers = this.createFormHeaders();
        this.addSIDCookie(headers, sid);

        var formParams = new LinkedMultiValueMap<>();
        formParams.add("hash", hash);

        var request = new HttpEntity<>(formParams, headers);

        var response = restTemplate.exchange(this.listUrl, HttpMethod.POST, request, new ParameterizedTypeReference<List<TorrentFile>>() {});
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Could not list torrents from torrent client, error code: " + response.getStatusCode());
        }

        return response.getBody();
    }

    @Override
    public void deleteTorrent(String sid, String hash, boolean removeFiles) {
        var headers = this.createFormHeaders();
        this.addSIDCookie(headers, sid);

        var formParams = new LinkedMultiValueMap<>();
        formParams.add("hashes", hash);
        formParams.add("deleteFiles", removeFiles);

        var request = new HttpEntity<>(formParams, headers);

        var response = restTemplate.exchange(this.deleteUrl, HttpMethod.POST, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Could not remove torrent from torrent client, error code: " + response.getStatusCode());
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
