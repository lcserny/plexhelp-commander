package net.cserny.core.torrent;

import net.cserny.api.dto.TorrentFile;

import java.util.List;

public interface TorrentRestClient {

    String generateSid();

    void addMagnet(String sid, String magnetUrl);

    List<TorrentFile> listTorrents(String sid, String hash);

    void deleteTorrent(String sid, String hash, boolean removeFiles);
}
