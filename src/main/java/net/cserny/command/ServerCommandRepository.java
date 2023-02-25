package net.cserny.command;

import io.quarkus.mongodb.panache.PanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class ServerCommandRepository implements PanacheMongoRepository<ServerCommand> {

    public void saveServer(ServerCommand serverCommand) {
        persist(serverCommand);
    }

    public Optional<ServerCommand> getByServerName(String serverName) {
        return Optional.ofNullable(find("serverName", serverName).firstResult());
    }

    public void setLastPingDate(String serverName, long timestamp) {
        update("lastPingDate", timestamp).where("serverName", serverName);
    }

    public void updateServerCommand(ServerCommand serverCommand) {
        update(serverCommand);
    }
}
