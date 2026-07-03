package net.cserny.core.torrent.qbittorrent;

import lombok.extern.slf4j.Slf4j;
import net.cserny.api.dto.Sid;
import net.cserny.api.dto.TorrentFile;
import net.cserny.api.TorrentRestClient;
import net.cserny.config.TorrentProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class QBitTorrentRestClient implements TorrentRestClient {

    private final RestTemplate restTemplate;

    private final String loginUrl;
    private final String addUrl;
    private final String listUrl;
    private final String deleteUrl;
    private final String username;
    private final String password;

    @Autowired
    public QBitTorrentRestClient(RestTemplate restTemplate, TorrentProperties properties) {
        this.restTemplate = restTemplate;
        this.username = properties.getUsername();
        this.password = properties.getPassword();
        this.loginUrl = properties.getBaseUrl() + "/api/v2/auth/login";
        this.addUrl = properties.getBaseUrl() + "/api/v2/torrents/add";
        this.listUrl = properties.getBaseUrl() + "/api/v2/torrents/files";
        this.deleteUrl = properties.getBaseUrl() + "/api/v2/torrents/delete";
    }

    @Override
    public Sid generateSid() {
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

        var fullSid = cookies.getFirst().substring(0, cookies.getFirst().indexOf(";")).trim();
        log.info("SID generated: {}", fullSid);

        String[] sidParts = fullSid.split("=");
        return new Sid(sidParts[0], sidParts[1]);
    }

    @Override
    public void addMagnet(Sid sid, String magnetUrl) {
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
    public List<TorrentFile> listTorrents(Sid sid, String hash) {
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
    public void deleteTorrent(Sid sid, String hash, boolean removeFiles) {
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

    private void addSIDCookie(HttpHeaders headers, Sid sid) {
        headers.set("Cookie", sid.name() + "=" + sid.value());
    }
}
