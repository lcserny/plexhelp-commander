package net.cserny.api;

import net.cserny.api.dto.TorrentFile;

import java.util.List;

public interface TorrentRestClient {

    void addMagnet(String magnetUrl);

    List<TorrentFile> listTorrents(String hash);

    void deleteTorrent(String hash, boolean removeFiles);
}
