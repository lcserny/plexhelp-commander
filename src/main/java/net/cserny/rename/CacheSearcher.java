package net.cserny.rename;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class CacheSearcher implements Searcher {

    @Inject
    OnlineCacheRepository repository;

    @Override
    public List<String> search(NameNormalizer.NameYear nameYear, MediaFileType type) {
        return null;
    }
}
