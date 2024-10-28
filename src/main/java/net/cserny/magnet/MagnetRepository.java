package net.cserny.magnet;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MagnetRepository extends MongoRepository<Magnet, ObjectId> {

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Page<Magnet> findAllByNamePattern(String name, Pageable pageable);
}
