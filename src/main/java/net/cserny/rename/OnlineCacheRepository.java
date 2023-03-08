package net.cserny.rename;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import net.cserny.rename.NameNormalizer.NameYear;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class OnlineCacheRepository implements PanacheMongoRepository<OnlineCacheItem> {

    public List<OnlineCacheItem> retrieveAllByNameYearAndType(NameYear nameYear, MediaFileType type) {
        if (nameYear.year() == null) {
            return retrieveAllByNameAndType(nameYear, type);
        }
        return list("searchName = ?1 and searchYear = ?2 and mediaType = ?3",
                nameYear.name(), nameYear.year(), type);
    }

    private List<OnlineCacheItem> retrieveAllByNameAndType(NameYear nameYear, MediaFileType type) {
        return list("searchName = ?1 and mediaType = ?2",
                nameYear.name(), type);
    }
}
