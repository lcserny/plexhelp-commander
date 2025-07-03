package net.cserny.magnet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Repository
public class MagnetRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    MagnetRepositoryInternal internalRepository;

    public Magnet save(Magnet magnet) {
        return internalRepository.save(magnet);
    }

    public List<Magnet> saveAll(List<Magnet> magnets) {
        return internalRepository.saveAll(magnets);
    }

    public Magnet findByHash(String hash) {
        return internalRepository.findByHash(hash);
    }

    public Page<Magnet> findAll(Pageable pageable) {
        return internalRepository.findAll(pageable);
    }

    public Page<Magnet> findAllByNamePattern(String name, Pageable pageable) {
        return internalRepository.findAllByNamePattern(name, pageable);
    }

    public Magnet findByHashAndUpdateDownloaded(String hash) {
        Query query = new Query(Criteria.where("hash").is(hash));
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
        Update update = new Update();
        update.set("downloaded", true);
        update.set("dateDownloaded", Instant.now(Clock.systemUTC()));

        return mongoTemplate.findAndModify(query, update, options, Magnet.class);
    }
}
