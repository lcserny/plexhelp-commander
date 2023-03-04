package net.cserny.rename;

import net.cserny.rename.OnlineResult.OnlineResultType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class OnlineSearcher {

    @Inject
    OnlineResultRepository repository;

    // TODO: inject and use TMDB

    public List<OnlineResult> findBy(String searchName, int searchYear, OnlineResultType type) {
        return null;
    }
}
