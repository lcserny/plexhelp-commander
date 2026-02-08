package net.cserny.core.move;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

interface MovedMediaRepository extends MongoRepository<MovedMedia, ObjectId> {

    List<MovedMedia> findByMediaName(String mediaName);
}
