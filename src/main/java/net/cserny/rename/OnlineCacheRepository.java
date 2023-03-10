package net.cserny.rename;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import net.cserny.rename.NameNormalizer.NameYear;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class OnlineCacheRepository implements PanacheMongoRepository<OnlineCacheItem> {

    private OnlineCacheItem convert(NameYear nameYear, MediaDescription description, MediaFileType mediaType) {
        OnlineCacheItem item = new OnlineCacheItem();
        item.searchName = nameYear.name();
        item.searchYear = nameYear.year();
        item.coverPath = description.posterUrl();
        item.title = description.title();
        item.date = description.date();
        item.description = description.description();
        item.cast = description.cast();
        item.mediaType = mediaType;
        return item;
    }

    public void saveOnlineCacheItem(NameYear nameYear, MediaDescription description, MediaFileType mediaType) {
        persist(convert(nameYear, description, mediaType));
    }

    public void saveAllOnlineCacheItem(NameYear nameYear, List<MediaDescription> descriptions, MediaFileType mediaType) {
        List<OnlineCacheItem> items = descriptions.stream()
                .map(description -> this.convert(nameYear, description, mediaType))
                .collect(Collectors.toList());
        persist(items);
    }

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
