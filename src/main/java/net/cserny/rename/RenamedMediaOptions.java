package net.cserny.rename;

import java.util.List;

public record RenamedMediaOptions(MediaRenameOrigin origin, List<MediaDescription> mediaDescriptions) {

    public boolean descriptionsFound() {
        return mediaDescriptions != null && !mediaDescriptions.isEmpty();
    }
}
