package net.cserny.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "rename")
public class RenameProperties {

    private List<String> trimRegex;
    private int similarityPercent;
    private int maxDepth;

    public List<String> getTrimRegex() {
        return trimRegex;
    }

    public void setTrimRegex(List<String> trimRegex) {
        this.trimRegex = trimRegex;
    }

    public int getSimilarityPercent() {
        return similarityPercent;
    }

    public void setSimilarityPercent(int similarityPercent) {
        this.similarityPercent = similarityPercent;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
}
