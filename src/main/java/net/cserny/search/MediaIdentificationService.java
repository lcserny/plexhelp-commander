package net.cserny.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
@Slf4j
public class MediaIdentificationService {

    @Autowired
    private SearchProperties searchConfig;

    public boolean isMedia(Path path) {
        if (!excludeConfiguredPaths(path)) {
            return false;
        }

        if (!excludeNonVideosByContentType(path)) {
            return false;
        }

        if (!excludeNonVideosBySize(path)) {
            return false;
        }

        return true;
    }

    private boolean excludeConfiguredPaths(Path path) {
        for (String excludePath : searchConfig.getExcludePaths()) {
            if (path.toAbsolutePath().toString().contains(excludePath)) {
                log.info("Excluding based on path: " + path.toString());
                return false;
            }
        }
        return true;
    }

    private boolean excludeNonVideosByContentType(Path path) {
        Optional<MediaType> mimeTypeOptional = MediaTypeFactory.getMediaType(path.toString());
        if (!mimeTypeOptional.isPresent()) {
            log.warn("Could not get content type of file " + path);
            return false;
        }

        String mimeType = mimeTypeOptional.get().toString();
        for (String allowedType : searchConfig.getVideoMimeTypes()) {
            if (allowedType.equals(mimeType)) {
                return true;
            }
        }

        var result = mimeType != null && mimeType.startsWith("video/");
        if (!result) {
            log.info("Excluding based on mime: " + path.toString() + " - " + mimeType);
        }

        return result;
    }

    private boolean excludeNonVideosBySize(Path path) {
        try {
            long size = Files.size(path);
            var result = size >= searchConfig.getVideoMinSizeBytes();
            if (!result) {
                log.info("Excluding based on size: " + path.toString() + " - " + size);
            }
            return result;
        } catch (IOException e) {
            log.warn("Could not get size of file " + path, e);
            return false;
        }
    }
}
