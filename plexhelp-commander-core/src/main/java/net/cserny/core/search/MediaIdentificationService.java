package net.cserny.core.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.MediaIdentifier;
import net.cserny.config.SearchProperties;
import net.cserny.fs.LocalFileService;
import net.cserny.fs.LocalPath;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
class MediaIdentificationService implements MediaIdentifier {

    private final LocalFileService localFileService;
    private final SearchProperties searchConfig;

    @Override
    public boolean isMedia(LocalPath path) {
        if (!localFileService.exists(path)) {
            log.info("Path doesn't exist: {}, skipping...", path);
            return false;
        }

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
