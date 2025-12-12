package net.cserny.download;

import net.cserny.support.DataMapper;
import net.cserny.download.internal.DownloadedMediaRepository;
import net.cserny.generated.DownloadedMediaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<DownloadedMediaData> retrieveAllPaginatedFrom(LocalDate date, List<String> names, Boolean downloaded, Pageable pageable) {
        Page<DownloadedMedia> mediaPage = repository.findAllPaginatedWith(date, downloaded, names, pageable);
        return mediaPage.map(DataMapper.INSTANCE::downloadedMediaToDownloadedMediaData);
    }
}
