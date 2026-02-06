package net.cserny.core.move.automove;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

interface AutoMoveMediaRepository extends MongoRepository<AutoMoveMedia, ObjectId> {
}
