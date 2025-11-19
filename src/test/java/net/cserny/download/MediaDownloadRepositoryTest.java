package net.cserny.download;

import net.cserny.MongoTestConfiguration;
import net.cserny.download.internal.DownloadedMediaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        DownloadedMediaRepository.class
})
@DataMongoTest
@Testcontainers
public class MediaDownloadRepositoryTest {

    @Autowired
    DownloadedMediaRepository repository;

    @Test
    @DisplayName("Check that repo retrieves correct downloaded media")
    void retrieveCorrectDownloadedMedia() {
        String name = "criteria media name";
        long size = 14L;
        LocalDateTime date = LocalDateTime.of(2013, 6, 5, 3, 40);
        Instant media1Date = date.atZone(ZoneOffset.UTC).toInstant();

        DownloadedMedia media = new DownloadedMedia();
        media.setFileName(name);
        media.setFileSize(size);
        media.setDateDownloaded(media1Date);
        media.setDownloadComplete(true);

        DownloadedMedia media2 = new DownloadedMedia();
        media2.setFileName(name);
        media2.setFileSize(size);
        media2.setDateDownloaded(date.plusDays(3).atZone(ZoneOffset.UTC).toInstant());
        media2.setDownloadComplete(false);

        repository.saveAll(List.of(media, media2));

        List<DownloadedMedia> list = repository.findAllWith(null, null, List.of(name));

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(name, list.getFirst().getFileName());
        assertEquals(size, list.getFirst().getFileSize());
        assertEquals(media1Date.toEpochMilli(), list.getFirst().getDateDownloaded().toEpochMilli());

        list = repository.findAllWith(null, true, List.of(name));

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(name, list.getFirst().getFileName());
        assertEquals(size, list.getFirst().getFileSize());
        assertEquals(media1Date.toEpochMilli(), list.getFirst().getDateDownloaded().toEpochMilli());

        list = repository.findAllWith(date.toLocalDate(), null, null);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(name, list.getFirst().getFileName());
        assertEquals(size, list.getFirst().getFileSize());
        assertEquals(media1Date.toEpochMilli(), list.getFirst().getDateDownloaded().toEpochMilli());

        list = repository.findAllWith(date.toLocalDate(), true, List.of(name));

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(name, list.getFirst().getFileName());
        assertEquals(size, list.getFirst().getFileSize());
        assertEquals(media1Date.toEpochMilli(), list.getFirst().getDateDownloaded().toEpochMilli());
    }
}
