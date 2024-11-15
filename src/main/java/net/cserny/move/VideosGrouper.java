package net.cserny.move;

import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;

public interface VideosGrouper {

    GroupedVideos group(MediaFileGroup fileGroup, MediaFileType type);
}
