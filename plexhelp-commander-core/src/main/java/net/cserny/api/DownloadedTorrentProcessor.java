package net.cserny.api;

import net.cserny.api.dto.TorrentFile;

import java.util.List;

public interface DownloadedTorrentProcessor {

    void addTorrents(List<TorrentFile> torrentFiles);
    void updateDownloaded(List<TorrentFile> torrentFiles);
}
