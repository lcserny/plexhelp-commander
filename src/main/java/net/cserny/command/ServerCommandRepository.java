package net.cserny.command;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ServerCommandRepository extends MongoRepository<ServerCommand, ObjectId> {

    @Query("{'serverName': { $eq: ?0 } }")
    Optional<ServerCommand> getByServerName(String serverName);
}
