package net.cserny.rename;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class OnlineSearcher implements Searcher {

    @Inject
    OnlineResultRepository repository;

    // TODO: inject and use TMDB

    @Override
    public List<String> search(NameNormalizer.NameYear nameYear, MediaFileType type) {
        return null;
    }
}
