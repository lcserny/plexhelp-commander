package net.cserny.download;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.List;

@Singleton
public class DownloadHistoryService {

    @Inject
    DownloadedMediaRepository repository;

    public List<DownloadedMedia> retrieveAllFromDate(LocalDate date) {
        return repository.list("date_downloaded >= ?1 and date_downloaded < ?2",
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }
}
