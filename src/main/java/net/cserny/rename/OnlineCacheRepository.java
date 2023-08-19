package net.cserny.rename;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface OnlineCacheRepository extends MongoRepository<OnlineCacheItem, ObjectId> {

    @Query("{'searchName' : { $eq: ?0 }, 'searchYear' : { $eq: ?1 }, 'mediaType' : { $eq: ?2 } }")
    List<OnlineCacheItem> findByNameYearAndType(String name, Integer year, MediaFileType type);

    @Query("{'searchName' : { $eq: ?0 }, 'mediaType' : { $eq: ?1 } }")
    List<OnlineCacheItem> findByNameAndType(String name, MediaFileType type);
}
