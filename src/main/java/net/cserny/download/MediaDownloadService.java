package net.cserny.download;

import net.cserny.DataMapper;
import net.cserny.generated.DownloadedMediaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MediaDownloadService {

    @Autowired
    DownloadedMediaRepository repository;

    public List<DownloadedMediaData> retrieveAllFrom(LocalDate date, List<String> names, Boolean downloaded) {
        List<DownloadedMedia> media = repository.findAllWith(date, downloaded, names);
        return media.stream().map(DataMapper.INSTANCE::downloadedMediaToDownloadedMediaData).toList();
    }
}
