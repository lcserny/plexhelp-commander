package net.cserny.command;

import net.cserny.support.BaseDocument;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

// clientName = "cloud" ??? - how to use multiplemongo client configs
@Document(collection = "server_commands")
public class ServerCommand extends BaseDocument {

    @Id
    public ObjectId id;
    public String serverName;
    public long lastPingDate;
    public List<String> actionsPending;
    public List<String> actionsAvailable;
}
