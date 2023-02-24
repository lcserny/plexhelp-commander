package net.cserny.command;

import io.quarkus.mongodb.panache.PanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServerCommandRepository implements PanacheMongoRepository<ServerCommand> {

    public ServerCommand getByServerName(String serverName) {
        return find("serverName", serverName).firstResult();
    }

    public void setLastPingDate(String serverName, long timestamp) {
        update("lastPingDate", timestamp).where("serverName", serverName);
    }

    public void updateServerCommand(ServerCommand serverCommand) {
        update(serverCommand);
    }
}
