package net.cserny.qtorrent;

import java.util.List;

public interface TorrentRestClient {

    String generateSid();

    void addMagnet(String sid, String magnetUrl);

    List<TorrentFile> listTorrents(String sid, String hash);

    void deleteTorrent(String sid, String hash, boolean removeFiles);
}
