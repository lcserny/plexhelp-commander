package net.cserny.search;

import java.util.List;

public class MediaSearchResponse {

    public List<MediaFile> mediaFiles;

    public MediaSearchResponse() {
    }

    public MediaSearchResponse(List<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }
}
