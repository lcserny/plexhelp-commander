package net.cserny.torrent;

public record TorrentFile(String name, long size, boolean isMedia) {
}
