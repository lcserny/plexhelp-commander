package net.cserny.core.download.internal;

import net.cserny.core.download.DownloadedMedia;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

interface InternalDownloadedMediaRepository extends MongoRepository<DownloadedMedia, ObjectId> {
}
