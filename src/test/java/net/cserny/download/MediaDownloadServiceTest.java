package net.cserny.download;

import net.cserny.generated.DownloadedMediaData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import net.cserny.MongoTestConfiguration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ContextConfiguration(classes = {
        DownloadedMediaRepository.class,
        MongoTestConfiguration.class,
        MediaDownloadService.class
})
@DataMongoTest
@Testcontainers
public class MediaDownloadServiceTest {

    @Autowired
    MediaDownloadService service;

    @Autowired
    DownloadedMediaRepository repository;

    @Test
    @DisplayName("Check that service retrieves correct media")
    void retrieveCorrectMedia() {
        String name = "hello";
        long size = 1L;
        LocalDateTime date = LocalDateTime.of(2010, 10, 1, 9, 33);
        Instant media1Date = date.atZone(ZoneOffset.UTC).toInstant();

        DownloadedMedia media = new DownloadedMedia();
        media.setFileName(name);
        media.setFileSize(size);
        media.setDateDownloaded(media1Date);

        DownloadedMedia media2 = new DownloadedMedia();
        media2.setFileName(name);
        media2.setFileSize(size);
        media2.setDateDownloaded(date.plusDays(3).atZone(ZoneOffset.UTC).toInstant());

        repository.saveAll(Arrays.asList(media, media2));

        List<DownloadedMediaData> list = service.retrieveAllFromDate(date.toLocalDate());

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(name, list.getFirst().getFileName());
        assertEquals(size, list.getFirst().getFileSize());
        assertEquals(media1Date.atOffset(ZoneOffset.UTC), list.getFirst().getDateDownloaded());
    }
}
