package net.cserny.core.torrent.qbittorrent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.dto.TorrentFile;
import net.cserny.api.TorrentRestClient;
import net.cserny.config.TorrentProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QBitTorrentRestClient implements TorrentRestClient {

    private final QBitTorrentRestApi restApi;
    private final TorrentProperties properties;

    @Override
    public void addMagnet(String magnetUrl) {
        var headers = newFormHeaders();
        addSidCookie(headers);

        var formParams = new LinkedMultiValueMap<String, String>();
        formParams.add("urls", magnetUrl);

        var response = restApi.torrentAdd(headers, formParams);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Could not add magnet to torrent client, error code: " + response.getStatusCode());
        }
    }

    @Override
    public List<TorrentFile> listTorrents(String hash) {
        var headers = newFormHeaders();
        addSidCookie(headers);

        var formParams = new LinkedMultiValueMap<String, String>();
        formParams.add("hash", hash);

        var response = restApi.torrentFiles(headers, formParams);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Could not list torrents from torrent client, error code: " + response.getStatusCode());
        }
        
        return response.getBody();
    }

    @Override
    public void deleteTorrent(String hash, boolean removeFiles) {
        var headers = newFormHeaders();
        addSidCookie(headers);

        var formParams = new LinkedMultiValueMap<String, String>();
        formParams.add("hashes", hash);
        formParams.add("deleteFiles", String.valueOf(removeFiles));

        var response = restApi.torrentDelete(headers, formParams);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Could not remove torrent from torrent client, error code: " + response.getStatusCode());
        }
    }

    private Sid generateSid() {
        var headers = newFormHeaders();
        var formParams = new LinkedMultiValueMap<String, String>();
        formParams.add("username", properties.getUsername());
        formParams.add("password", properties.getPassword());

        var response = restApi.authLogin(headers, formParams);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Could not login to torrent client, error code: " + response.getStatusCode());
        }

        var cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies == null || cookies.isEmpty() || !cookies.getFirst().contains("SID")) {
            throw new RestClientException("No SID found in response cookies");
        }

        var fullSid = cookies.getFirst().substring(0, cookies.getFirst().indexOf(";")).trim();
        log.info("SID generated: {}", fullSid);

        String[] sidParts = fullSid.split("=");
        return new Sid(sidParts[0], sidParts[1]);
    }

    private HttpHeaders newFormHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private void addSidCookie(HttpHeaders headers) {
        Sid sid = generateSid();
        headers.set(HttpHeaders.COOKIE, sid.name() + "=" + sid.value());
    }

    record Sid(String name, String value) {}
}
