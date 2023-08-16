package net.cserny.rename;

import net.cserny.rename.NameNormalizer.NameYear;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.stream.Collectors;

public interface OnlineCacheRepository extends MongoRepository<OnlineCacheItem, ObjectId> {

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

    default void saveOnlineCacheItem(NameYear nameYear, MediaDescription description, MediaFileType mediaType) {
        save(convert(nameYear, description, mediaType));
    }

    default void saveAllOnlineCacheItem(NameYear nameYear, List<MediaDescription> descriptions, MediaFileType mediaType) {
        List<OnlineCacheItem> items = descriptions.stream()
                .map(description -> this.convert(nameYear, description, mediaType))
                .collect(Collectors.toList());
        saveAll(items);
    }

    default List<OnlineCacheItem> autoRetrieveAllByNameYearAndType(NameYear nameYear, MediaFileType type) {
        if (nameYear.year() == null) {
            return retrieveAllByNameAndType(nameYear.name(), type);
        }
        return retrieveAllByNameYearAndType(nameYear.name(), nameYear.year(), type);
    }

    @Query("{'searchName' : { $eq: ?0 }, 'searchYear' : { $eq: ?1 }, 'mediaType' : { $eq: ?2 } }")
    List<OnlineCacheItem> retrieveAllByNameYearAndType(String name, Integer year, MediaFileType type);

    @Query("{'searchName' : { $eq: ?0 }, 'mediaType' : { $eq: ?1 } }")
    List<OnlineCacheItem> retrieveAllByNameAndType(String name, MediaFileType type);
}
