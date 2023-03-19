package net.cserny.download;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.List;

@Singleton
public class MediaDownloadService {

    @Inject
    DownloadedMediaRepository repository;

    public List<DownloadedMedia> retrieveAllFromDate(LocalDate date) {
        return repository.retrieveAllFromDate(date);
    }
}
