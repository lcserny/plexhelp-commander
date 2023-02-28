package net.cserny.download;

import io.quarkus.mongodb.panache.PanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class DownloadedMediaRepository implements PanacheMongoRepository<DownloadedMedia> {

    public List<DownloadedMedia> retrieveAllFromDate(LocalDate date) {
        return list("date_downloaded >= ?1 and date_downloaded < ?2",
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }
}
