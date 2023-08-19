package net.cserny.search;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "search")
public class SearchProperties {

    private int maxDepth;
    private List<String> excludePaths;
    private long videoMinSizeBytes;
    private List<String> videoMimeTypes;

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }

    public long getVideoMinSizeBytes() {
        return videoMinSizeBytes;
    }

    public void setVideoMinSizeBytes(long videoMinSizeBytes) {
        this.videoMinSizeBytes = videoMinSizeBytes;
    }

    public List<String> getVideoMimeTypes() {
        return videoMimeTypes;
    }

    public void setVideoMimeTypes(List<String> videoMimeTypes) {
        this.videoMimeTypes = videoMimeTypes;
    }
}
