package net.cserny.api;

import net.cserny.generated.MediaFileGroup;

import java.util.List;

public interface MediaFileGroupGenerator {

    List<MediaFileGroup> generateMediaFileGroupsFromDownloads(List<String> relativeMediaPaths);
}
