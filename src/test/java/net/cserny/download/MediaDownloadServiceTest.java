package net.cserny.download;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest({
        "server.command.name=test-server",
        "server.command.listen-cron=disabled",
        "search.video-min-size-bytes=5",
        "search.exclude-paths[0]=Excluded Folder 1"
})
@ContextConfiguration(classes = {
        DownloadedMediaRepository.class,
        MediaDownloadService.class
})
@EnableAutoConfiguration
@EnableMongoRepositories
@Testcontainers
public class MediaDownloadServiceTest {

    @Container
    public static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:5.0");

    @DynamicPropertySource
    public static void qTorrentProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoContainer.getConnectionString());
    }

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

        List<DownloadedMedia> list = service.retrieveAllFromDate(date.toLocalDate());

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(name, list.get(0).getFileName());
        assertEquals(size, list.get(0).getFileSize());
        assertEquals(media1Date, list.get(0).getDateDownloaded());
    }
}
