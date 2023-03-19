package net.cserny.download;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.cserny.MongoTestSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@Testcontainers
@QuarkusTestResource(MongoTestSetup.class)
public class MediaDownloadServiceTest {

    @Inject
    MediaDownloadService service;

    @Inject
    DownloadedMediaRepository repository;

    @Test
    @DisplayName("Check that service retrieves correct media")
    void retrieveCorrectMedia() {
        String name = "hello";
        long size = 1L;
        LocalDateTime date = LocalDateTime.of(2010, 10, 1, 9, 33);

        DownloadedMedia media = new DownloadedMedia();
        media.fileName = name;
        media.fileSize = size;
        media.dateDownloaded = date;

        DownloadedMedia media2 = new DownloadedMedia();
        media2.fileName = name;
        media2.fileSize = size;
        media2.dateDownloaded = date.plusDays(3);

        repository.persist(Arrays.asList(media, media2));

        List<DownloadedMedia> list = service.retrieveAllFromDate(date.toLocalDate());

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(name, list.get(0).fileName);
        assertEquals(size, list.get(0).fileSize);
        assertEquals(date, list.get(0).dateDownloaded);
    }
}
