package net.cserny.rename;

import io.quarkus.mongodb.panache.PanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OnlineResultRepository implements PanacheMongoRepository<OnlineResult> {
}