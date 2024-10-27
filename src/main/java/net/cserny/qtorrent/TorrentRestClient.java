package net.cserny.qtorrent;

public interface TorrentRestClient {

    String generateSid();

    void addMagnet(String sid, String magnetUrl);
}
