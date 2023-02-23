package net.cserny.shutdown;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "command", clientName = "cloud")
public class Servers {
}
