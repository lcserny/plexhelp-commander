package net.cserny.rename;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "online")
public class OnlineConfig {

    private int resultLimit;
    private String posterBase;

    public int getResultLimit() {
        return resultLimit;
    }

    public void setResultLimit(int resultLimit) {
        this.resultLimit = resultLimit;
    }

    public String getPosterBase() {
        return posterBase;
    }

    public void setPosterBase(String posterBase) {
        this.posterBase = posterBase;
    }
}
