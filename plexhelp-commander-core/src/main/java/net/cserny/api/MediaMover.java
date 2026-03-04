package net.cserny.api;

import net.cserny.api.dto.LocalPath;
import net.cserny.api.dto.MediaInfo;
import net.cserny.generated.MediaDescriptionData;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaMoveError;

import java.util.List;

public interface MediaMover {

    List<MediaMoveError> moveMedia(MediaFileGroup fileGroup, MediaFileType type, MediaDescriptionData mediaDesc);

    // TODO existsBy...

    void persistMovedMedia(LocalPath srcPath, LocalPath destPath, MediaInfo mediaInfo, MediaDescriptionData mediaDesc);
}
