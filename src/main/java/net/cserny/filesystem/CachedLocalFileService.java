package net.cserny.filesystem;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.cserny.LRUCache;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Slf4j
@Getter
public class CachedLocalFileService extends LocalFileService {

    private static final int CACHE_LIMIT = 10000;

    private final LRUCache<String, BasicFileAttributes> fileAttrCache = new LRUCache<>(CACHE_LIMIT);

    public CachedLocalFileService(FileSystem fileSystem) {
        super(fileSystem);
    }

    // FIXME: buggy impl, if parent folder is scanned and contains X items first time, doesn't matter if more items
    //  are added in that folder later, folder data is taken from cache and it can't see new items
    //  Maybe check dateModified when getting from cache, if different, getRealAttr and update cache
    //  but this might make the cache useless / not worth it...
    public LocalPath toLocalPath(Path path, BasicFileAttributes attr) {
        String key = path.toAbsolutePath().toString();

        if (attr == null) {
            attr = fileAttrCache.get(key);
            if (attr == null) {
                attr = getRealAttributes(path);
            }
        }
        fileAttrCache.put(key, attr);

        return new LocalPath(path, attr);
    }
}
