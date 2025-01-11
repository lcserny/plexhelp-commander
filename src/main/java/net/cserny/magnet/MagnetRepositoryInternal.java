package net.cserny.magnet;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

interface MagnetRepositoryInternal extends MongoRepository<Magnet, ObjectId> {

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Page<Magnet> findAllByNamePattern(String name, Pageable pageable);

    @Query("{ 'hash': { $eq: ?0 } }")
    Magnet findByHash(String hash);
}
