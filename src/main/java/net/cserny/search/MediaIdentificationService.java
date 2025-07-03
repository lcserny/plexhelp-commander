package net.cserny.search;

import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.LocalPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class MediaIdentificationService {

    @Autowired
    private SearchProperties searchConfig;

    public boolean isMedia(LocalPath path) {
        if (!excludeNonVideosByContentType(path)) {
            return false;
        }

        return excludeNonVideosBySize(path);
    }

    private boolean excludeNonVideosByContentType(LocalPath path) {
        if (path.attributes().isDirectory()) {
            return false;
        }

        Optional<MediaType> mimeTypeOptional = MediaTypeFactory.getMediaType(path.toString());
        if (mimeTypeOptional.isEmpty()) {
            log.warn("Could not get content type of file {}", path);
            return false;
        }

        String mimeType = mimeTypeOptional.get().toString();
        for (String allowedType : searchConfig.getVideoMimeTypes()) {
            if (allowedType.equals(mimeType)) {
                return true;
            }
        }

        var result = mimeType.startsWith("video/");
        if (!result) {
            log.debug("Excluding based on mime: {} - {}", path, mimeType);
        }

        return result;
    }

    private boolean excludeNonVideosBySize(LocalPath path) {
        if (path.attributes().isDirectory()) {
            return false;
        }

        var result = path.attributes().size() >= searchConfig.getVideoMinSizeBytes();
        if (!result) {
            log.debug("Excluding based on size: {} - {}", path, path.attributes().size());
        }
        return result;
    }
}
