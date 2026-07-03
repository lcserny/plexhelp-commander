package net.cserny.api;

import net.cserny.api.dto.Sid;
import net.cserny.api.dto.TorrentFile;

import java.util.List;

public interface TorrentRestClient {

    Sid generateSid();

    void addMagnet(Sid sid, String magnetUrl);

    List<TorrentFile> listTorrents(Sid sid, String hash);

    void deleteTorrent(Sid sid, String hash, boolean removeFiles);
}
