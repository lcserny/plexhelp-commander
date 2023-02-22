package net.cserny.search;

import net.cserny.search.OnlineResult.OnlineResultType;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class OnlineSearchService {

    @Inject
    OnlineResultRepository repository;

    // TODO: inject and use TMDB

    public List<OnlineResult> findBy(String searchName, int searchYear, OnlineResultType type) {
        return null;
    }
}
