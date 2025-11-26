package net.cserny.filesystem;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.Features;
import net.cserny.LRUCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.listener.FeatureStateChangedListener;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Function;

@RequiredArgsConstructor
@Slf4j
@Component
public class CachedLocalPathProvider implements FeatureStateChangedListener {

    private static final int CACHE_LIMIT = 10000;

    @Getter
    private volatile boolean filesystemCacheEnabled;
    private volatile boolean filesystemCacheLoggingEnabled;

    private final LRUCache<String, BasicFileAttributes> fileAttrCache = new LRUCache<>(CACHE_LIMIT);
    private final FeatureManager featureManager;

    @PostConstruct
    public void init() {
        this.filesystemCacheEnabled = featureManager.isActive(Features.FILESYSTEM_CACHE);
    }

    // FIXME: buggy impl, if parent folder is scanned and contains X items first time, doesn't matter if more items
    //  are added in that folder later, folder data is taken from cache and it can't see new items
    //  Maybe check dateModified when getting from cache, if different, getRealAttr and update cache
    //  but this might make the cache useless / not worth it...
    public LocalPath toLocalPath(Path path, BasicFileAttributes attr, Function<Path, BasicFileAttributes> attrSupplier) {
        String key = path.toAbsolutePath().toString();

        if (attr == null) {
            attr = fileAttrCache.get(key);
            if (attr == null) {
                attr = attrSupplier.apply(path);
            }
        }
        fileAttrCache.put(key, attr);

        return new LocalPath(path, attr);
    }

    @Scheduled(cron = "${filesystem.cache.cron}")
    public void runCacheLogging() {
        if (filesystemCacheLoggingEnabled) {
            log.info("Current FileService cache size {} and cacheMisses {}", fileAttrCache.size(), fileAttrCache.cacheMisses());
        }
    }

    @Override
    public void onFeatureStateChanged(FeatureState fromState, FeatureState toState) {
        if (toState.getFeature().name().equals(Features.FILESYSTEM_CACHE.name())) {
            this.filesystemCacheEnabled = toState.isEnabled();
        }

        if (toState.getFeature().name().equals(Features.FILESYSTEM_CACHE_LOGGING.name())) {
            this.filesystemCacheLoggingEnabled = toState.isEnabled();
        }
    }

    @Override
    public int priority() {
        return 0;
    }
}
