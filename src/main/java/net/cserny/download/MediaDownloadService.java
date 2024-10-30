package net.cserny.download;

import net.cserny.DataMapper;
import net.cserny.generated.DownloadedMediaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class MediaDownloadService {

    @Autowired
    DownloadedMediaRepository repository;

    public List<DownloadedMediaData> retrieveAllFromDate(LocalDate date) {
        return repository.retrieveAllFromDate(
                        date.atStartOfDay(ZoneOffset.UTC).toInstant(),
                        date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant())
                .stream().map(DataMapper.INSTANCE::downloadedMediaToDownloadedMediaData)
                .toList();
    }
}
