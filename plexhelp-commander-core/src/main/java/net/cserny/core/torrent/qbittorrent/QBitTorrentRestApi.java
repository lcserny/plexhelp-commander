package net.cserny.core.torrent.qbittorrent;

import net.cserny.api.dto.TorrentFile;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange(QBitTorrentRestApi.Routes.ROOT_URI)
public interface QBitTorrentRestApi {

    @PostExchange(Routes.TORRENT_ADD_URI)
    void torrentAdd(@RequestPart("urls") String urls);

    @PostExchange(Routes.TORRENT_FILES_URI)
    List<TorrentFile> torrentFiles(@RequestPart("hash") String hash);

    @PostExchange(Routes.TORRENT_DELETE_URI)
    void torrentDelete(@RequestPart("hashes") String hashes,
                       @RequestPart("deleteFiles") boolean deleteFiles);

    interface Routes {
        String ROOT_URI = "/api/v2";
        String AUTH_LOGIN_URI = "/auth/login";
        String TORRENT_ADD_URI = "/torrents/add";
        String TORRENT_DELETE_URI = "/torrents/delete";
        String TORRENT_FILES_URI = "/torrents/files";
    }
}
