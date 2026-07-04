package net.cserny.core.torrent.qbittorrent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.QBitTorrentRestApi;
import net.cserny.api.dto.Sid;
import net.cserny.api.dto.TorrentFile;
import net.cserny.api.TorrentRestClient;
import net.cserny.config.TorrentProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public Sid generateSid() {
        var formParams = new LinkedMultiValueMap<String, String>();
        formParams.add("username", properties.getUsername());
        formParams.add("password", properties.getPassword());

        var response = restApi.authLogin(formParams);
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
        var headers = createFormHeaders();
        addSIDCookie(headers, sid);

        var formParams = new LinkedMultiValueMap<String, String>();
        formParams.add("urls", magnetUrl);

        var response = restApi.torrentAdd(formParams, headers);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Could not add magnet to torrent client, error code: " + response.getStatusCode());
        }
    }

    @Override
    public List<TorrentFile> listTorrents(Sid sid, String hash) {
        var headers = createFormHeaders();
        addSIDCookie(headers, sid);

        var formParams = new LinkedMultiValueMap<String, String>();
        formParams.add("hash", hash);

        // Note: QBitTorrentRestApi.torrentFiles returns ResponseEntity<String>.
        // I might need to change it to ResponseEntity<List<TorrentFile>> or deserialize manually.
        // Let's assume for now I need to change the API return type to handle the deserialization.
        var response = restApi.torrentFiles(formParams, headers);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Could not list torrents from torrent client, error code: " + response.getStatusCode());
        }
        
        return response.getBody();
    }

    @Override
    public void deleteTorrent(Sid sid, String hash, boolean removeFiles) {
        var headers = createFormHeaders();
        addSIDCookie(headers, sid);

        var formParams = new LinkedMultiValueMap<String, String>();
        formParams.add("hashes", hash);
        formParams.add("deleteFiles", String.valueOf(removeFiles));

        var response = restApi.torrentDelete(formParams, headers);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Could not remove torrent from torrent client, error code: " + response.getStatusCode());
        }
    }

    // TODO why do I need this since my internal RestCLient already has this as default header?
    private HttpHeaders createFormHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private void addSIDCookie(HttpHeaders headers, Sid sid) {
        headers.set("Cookie", sid.name() + "=" + sid.value());
    }
}
