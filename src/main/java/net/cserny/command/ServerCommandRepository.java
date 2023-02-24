package net.cserny.command;

import io.quarkus.mongodb.panache.PanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServerCommandRepository implements PanacheMongoRepository<ServerCommand> {
}
