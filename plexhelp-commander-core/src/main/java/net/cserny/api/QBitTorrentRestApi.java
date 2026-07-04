package net.cserny.api;

import net.cserny.api.dto.TorrentFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange(QBitTorrentRestApi.Routes.BASE_URI)
public interface QBitTorrentRestApi {

    @PostExchange(Routes.AUTH_LOGIN_URI)
    ResponseEntity<Void> authLogin(@RequestBody MultiValueMap<String, String> formParams);

    @PostExchange(Routes.TORRENT_ADD_URI)
    ResponseEntity<Void> torrentAdd(@RequestBody MultiValueMap<String, String> formParams,
                                    @RequestHeader HttpHeaders headers);

    @PostExchange(Routes.TORRENT_FILES_URI)
    ResponseEntity<List<TorrentFile>> torrentFiles(@RequestBody MultiValueMap<String, String> formParams,
                                                   @RequestHeader HttpHeaders headers);

    @PostExchange(Routes.TORRENT_DELETE_URI)
    ResponseEntity<Void> torrentDelete(@RequestBody MultiValueMap<String, String> formParams,
                                       @RequestHeader HttpHeaders headers);

    interface Routes {

        String BASE_URI = "/api/v2";
        String AUTH_LOGIN_URI = "/auth/login";
        String TORRENT_ADD_URI = "/torrents/add";
        String TORRENT_DELETE_URI = "/torrents/delete";
        String TORRENT_FILES_URI = "/torrents/files";
    }
}
