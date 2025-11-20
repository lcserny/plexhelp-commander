package net.cserny.move;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

public interface AutoMoveMediaRepository extends MongoRepository<AutoMoveMedia, ObjectId> {
}
