package net.cserny.download;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

interface InternalDownloadedMediaRepository extends MongoRepository<DownloadedMedia, ObjectId> {
}
