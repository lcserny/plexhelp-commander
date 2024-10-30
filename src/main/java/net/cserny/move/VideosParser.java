package net.cserny.move;

import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;

public interface VideosParser {

    ParsedVideos parse(MediaFileGroup fileGroup, MediaFileType type);
}
