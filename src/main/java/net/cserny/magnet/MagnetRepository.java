package net.cserny.magnet;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MagnetRepository extends MongoRepository<Magnet, ObjectId> {
}
