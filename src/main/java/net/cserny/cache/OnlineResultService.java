package net.cserny.cache;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class OnlineResultService {

    @Inject
    OnlineResultRepository repository;

    // TODO: inject and use TMDB

    public List<OnlineResult> findBy(String searchName, int searchYear, OnlineResultType type) {
        return null;
    }
}
