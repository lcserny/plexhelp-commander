package net.cserny.download;

import io.quarkus.logging.Log;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Dependent
public class DownloadHistoryService {

    @Inject
    DownloadedMediaRepository repository;

    public List<DownloadedMedia> retrieveAllFromDate(LocalDate date) {
        return repository.list("date_downloaded >= ?1 and date_downloaded < ?2",
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }
}
