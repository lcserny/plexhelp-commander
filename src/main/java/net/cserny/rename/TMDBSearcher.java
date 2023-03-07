package net.cserny.rename;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Priority(0)
@Singleton
public class TMDBSearcher implements Searcher {

    @Inject
    OnlineCacheRepository repository;

    // TODO: inject tmdb and use

    @Override
    public RenamedMediaOptions search(NameNormalizer.NameYear nameYear, MediaFileType type) {
        return null;
    }
}
