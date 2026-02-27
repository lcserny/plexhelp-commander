package net.cserny.task.subs;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

interface SubtitleReducedMediaRepository extends MongoRepository<SubtitleReducedMedia, ObjectId> {

    @Query("{ 'filePath': { $eq: ?0 } }")
    Optional<SubtitleReducedMedia> findByFilePath(String filePath);
}
