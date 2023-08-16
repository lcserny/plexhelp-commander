package net.cserny.rename;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "rename")
public class RenameConfig {

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
