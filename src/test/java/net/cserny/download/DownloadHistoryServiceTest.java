package net.cserny.download;

import io.quarkus.test.junit.QuarkusTest;
import net.cserny.MongoDockerExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@Testcontainers
public class DownloadHistoryServiceTest {

    @RegisterExtension
    static final MongoDockerExtension deploy = new MongoDockerExtension();

    @Inject
    DownloadHistoryService service;

    @Inject
    DownloadedMediaRepository repository;

    @Test
    @DisplayName("Check that service retrieves correct media")
    void retrieveCorrectMedia() {
        String name = "name";
        long size = 1L;
        LocalDateTime date = LocalDateTime.of(2000, 10, 1, 9, 33);

        DownloadedMedia media = new DownloadedMedia();
        media.fileName = name;
        media.fileSize = size;
        media.dateDownloaded = date;

        repository.persist(media);
        List<DownloadedMedia> list = service.retrieveAllFromDate(date.toLocalDate());

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(name, list.get(0).fileName);
        assertEquals(size, list.get(0).fileSize);
        assertEquals(date, list.get(0).dateDownloaded);
    }
}
