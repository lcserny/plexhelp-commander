package net.cserny.move;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "move")
public class MoveConfig {

    private List<String> restrictedRemovePaths;
    private List<String> subsExt;
    private int subsMaxDepth;

    public List<String> getRestrictedRemovePaths() {
        return restrictedRemovePaths;
    }

    public void setRestrictedRemovePaths(List<String> restrictedRemovePaths) {
        this.restrictedRemovePaths = restrictedRemovePaths;
    }

    public List<String> getSubsExt() {
        return subsExt;
    }

    public void setSubsExt(List<String> subsExt) {
        this.subsExt = subsExt;
    }

    public int getSubsMaxDepth() {
        return subsMaxDepth;
    }

    public void setSubsMaxDepth(int subsMaxDepth) {
        this.subsMaxDepth = subsMaxDepth;
    }
}
