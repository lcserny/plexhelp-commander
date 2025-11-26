package net.cserny;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.listener.FeatureStateChangedListener;

@RequiredArgsConstructor
@Component
@Slf4j
public class FeatureConfiguration implements FeatureStateChangedListener {

    private final FeatureManager featureManager;

    @Getter
    private volatile boolean filesystemCacheEnabled;
    @Getter
    private volatile boolean filesystemCacheLoggingEnabled;
    @Getter
    private volatile boolean automoveCronEnabled;

    @PostConstruct
    public void init() {
        filesystemCacheEnabled = featureManager.isActive(Features.FILESYSTEM_CACHE);
        filesystemCacheLoggingEnabled = featureManager.isActive(Features.FILESYSTEM_CACHE_LOGGING);
        automoveCronEnabled = featureManager.isActive(Features.AUTOMOVE);
    }

    @Override
    public void onFeatureStateChanged(FeatureState fromState, FeatureState toState) {
        log.info("Feature state changed for {} from {} to {}", fromState.getFeature(), fromState.isEnabled(), toState.isEnabled());

        if (toState.getFeature().name().equals(Features.FILESYSTEM_CACHE.name())) {
            this.filesystemCacheEnabled = toState.isEnabled();
        }

        if (toState.getFeature().name().equals(Features.FILESYSTEM_CACHE_LOGGING.name())) {
            this.filesystemCacheLoggingEnabled = toState.isEnabled();
        }

        if (toState.getFeature().name().equals(Features.AUTOMOVE.name())) {
            this.automoveCronEnabled = toState.isEnabled();
        }
    }

    @Override
    public int priority() {
        return 0;
    }
}
