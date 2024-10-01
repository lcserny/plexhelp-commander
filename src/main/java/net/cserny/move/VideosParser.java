package net.cserny.move;

import net.cserny.rename.MediaFileType;
import net.cserny.search.MediaFileGroup;

public interface VideosParser {

    ParsedVideos parse(MediaFileGroup fileGroup, MediaFileType type);
}
