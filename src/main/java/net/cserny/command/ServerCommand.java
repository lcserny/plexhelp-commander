package net.cserny.command;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.util.List;

@MongoEntity(collection = "server_commands", clientName = "cloud")
public class ServerCommand {

    public ObjectId id;
    public String serverName;
    public long lastPingDate;
    public List<String> actionsPending;
    public List<String> actionsAvailable;
}
