package net.cserny.download;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MediaDownloadService {

    @Autowired
    DownloadedMediaRepository repository;

    public List<DownloadedMedia> retrieveAllFromDate(LocalDate date) {
        return repository.retrieveAllFromDate(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }
}
