package net.cserny.qtorrent;

public enum TorrentOperation {

    ADDED("added"),
    DOWNLOADED("downloaded");

    private final String operation;

    TorrentOperation(String operation) {
        this.operation = operation;
    }

    public static TorrentOperation fromString(String operation) {
        for (TorrentOperation op : TorrentOperation.values()) {
            if (op.operation.equals(operation)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown torrent operation: " + operation);
    }
}
