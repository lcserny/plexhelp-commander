package net.cserny.download;

import io.quarkus.mongodb.panache.PanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DownloadedMediaRepository implements PanacheMongoRepository<DownloadedMedia> {
}
