package net.cserny.support.events;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.cserny.torrent.TorrentFile;

import java.util.List;

public class Events {

    public static abstract class CommanderEvent {}

    @Builder
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class TorrentDownloadStarted extends CommanderEvent {

        @NonNull
        private final String hash;

        @Valid
        @NonNull
        private final List<TorrentFile>  torrentFiles;
    }

    @Builder
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class TorrentDownloadCompleted extends CommanderEvent {

        @NonNull
        private final String hash;

        @Valid
        @NonNull
        private final List<TorrentFile>  torrentFiles;
    }
}
