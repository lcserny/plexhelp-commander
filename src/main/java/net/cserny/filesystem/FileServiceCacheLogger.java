package net.cserny.filesystem;

import lombok.extern.slf4j.Slf4j;
import net.cserny.LRUCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.attribute.BasicFileAttributes;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "filesystem.cache", name = {"enabled", "logger-enabled"}, havingValue = "true")
public class FileServiceCacheLogger {

    @Autowired
    private LocalFileService fileService;

    @Scheduled(initialDelayString = "${filesystem.cache.initial-delay-ms}", fixedDelayString = "${filesystem.cache.cron-ms}")
    public void runCacheLogging() {
        LRUCache<String, BasicFileAttributes> fileAttrCache = fileService.getFileAttrCache();
        log.info("Current FileService cache size {} and cacheMisses {}", fileAttrCache.size(), fileAttrCache.cacheMisses());
    }
}
