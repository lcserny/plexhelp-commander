package net.cserny.rename;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Priority(1)
@Singleton
public class CacheSearcher implements Searcher {

    @Inject
    OnlineCacheRepository repository;

    @Override
    public RenamedMediaOptions search(NameNormalizer.NameYear nameYear, MediaFileType type) {
        return null;
    }
}
