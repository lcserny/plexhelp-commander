package net.cserny.filesystem;

import lombok.extern.slf4j.Slf4j;
import net.cserny.LRUCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.nio.file.attribute.BasicFileAttributes;

@Slf4j
public class FileServiceCacheLogger {

    private CachedLocalFileService fileService = null;

    @Autowired
    public FileServiceCacheLogger(LocalFileService fileService) {
        if (fileService instanceof CachedLocalFileService) {
            this.fileService = (CachedLocalFileService) fileService;
        }
    }

    @Scheduled(initialDelayString = "${filesystem.cache.initial-delay-ms}", fixedDelayString = "${filesystem.cache.cron-ms}")
    public void runCacheLogging() {
        if (this.fileService == null) {
            return;
        }

        LRUCache<String, BasicFileAttributes> fileAttrCache = fileService.getFileAttrCache();
        log.info("Current FileService cache size {} and cacheMisses {}", fileAttrCache.size(), fileAttrCache.cacheMisses());
    }
}
