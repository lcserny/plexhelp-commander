package net.cserny.rename;

import net.cserny.rename.OnlineResult.OnlineResultType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class OnlineSearchService {

    @Inject
    OnlineResultRepository repository;

    // TODO: inject and use TMDB

    public List<OnlineResult> findBy(String searchName, int searchYear, OnlineResultType type) {
        return null;
    }
}
