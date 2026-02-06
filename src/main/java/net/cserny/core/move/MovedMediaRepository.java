package net.cserny.core.move;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

interface MovedMediaRepository extends MongoRepository<MovedMedia, ObjectId> {
}
