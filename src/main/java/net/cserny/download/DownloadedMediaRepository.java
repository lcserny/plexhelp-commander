package net.cserny.download;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface DownloadedMediaRepository extends MongoRepository<DownloadedMedia, ObjectId> {

    @Query("{'dateDownloaded' : { $gte: ?0, $lt: ?1 } }")
    List<DownloadedMedia> retrieveAllFromDate(Instant from, Instant to);
}
