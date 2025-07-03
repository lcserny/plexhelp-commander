package net.cserny.move;

import net.cserny.filesystem.LocalPath;

import java.util.List;

public record GroupedVideos(List<String> videos, List<LocalPath> deletableVideos) {
}
