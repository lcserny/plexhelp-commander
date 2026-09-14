package net.cserny.core.download;

import net.cserny.IntegrationTest;
import net.cserny.core.download.DownloadedMedia;
import net.cserny.core.download.internal.DownloadedMediaRepository;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MediaDownloadRepositoryTest extends IntegrationTest {

    @Autowired
    DownloadedMediaRepository repository;

    @Autowired
    MongoTemplate mongoTemplate;

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

    @Test
    @DisplayName("findForAutoMove returns only completed media that was not tried yet")
    void findForAutoMoveReturnsOnlyCompletedUntried() {
        mongoTemplate.remove(new Query(), DownloadedMedia.class);

        DownloadedMedia completed = new DownloadedMedia();
        completed.setFileName("completed.mkv");
        completed.setFileSize(6);
        completed.setDateDownloaded(Instant.now());
        completed.setDownloadComplete(true);
        completed.setTriedAutoMove(false);

        DownloadedMedia incomplete = new DownloadedMedia();
        incomplete.setFileName("incomplete.mkv");
        incomplete.setFileSize(6);
        incomplete.setDateDownloaded(Instant.now());
        incomplete.setDownloadComplete(false);
        incomplete.setTriedAutoMove(false);

        DownloadedMedia alreadyTried = new DownloadedMedia();
        alreadyTried.setFileName("already-tried.mkv");
        alreadyTried.setFileSize(6);
        alreadyTried.setDateDownloaded(Instant.now());
        alreadyTried.setDownloadComplete(true);
        alreadyTried.setTriedAutoMove(true);

        repository.saveAll(List.of(completed, incomplete, alreadyTried));

        List<DownloadedMedia> list = repository.findForAutoMove(10);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("completed.mkv", list.getFirst().getFileName());
    }

    @Test
    @DisplayName("findForAutoMove ignores documents with unknown download state")
    void findForAutoMoveIgnoresUnknownDownloadState() {
        mongoTemplate.remove(new Query(), DownloadedMedia.class);
        mongoTemplate.insert(new Document("file_name", "legacy.mkv")
                .append("file_size", 6)
                .append("date_downloaded", Instant.now()), "download_cache");

        List<DownloadedMedia> list = repository.findForAutoMove(10);

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }
}
