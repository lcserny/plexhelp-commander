package net.cserny.move;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MovedMediaRepository extends MongoRepository<MovedMedia, ObjectId> {
}
