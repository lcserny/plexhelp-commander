package net.cserny.download.internal;

import net.cserny.download.DownloadedMedia;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

interface InternalDownloadedMediaRepository extends MongoRepository<DownloadedMedia, ObjectId> {
}
