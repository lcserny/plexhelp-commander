package net.cserny.move;

import net.cserny.IntegrationTest;
import net.cserny.download.DownloadedMedia;
import net.cserny.download.internal.DownloadedMediaRepository;
import net.cserny.filesystem.FilesystemProperties;
import net.cserny.generated.MediaFileType;
import net.cserny.rename.OnlineCacheItem;
import net.cserny.rename.internal.OnlineCacheRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.junit.jupiter.api.Assertions.*;

class AutoMoveMediaServiceTest extends IntegrationTest {

    @Autowired
    AutoMoveMediaService service;

    @Autowired
    DownloadedMediaRepository downloadedMediaRepository;

    @Autowired
    AutoMoveMediaRepository autoMoveMediaRepository;

    @Autowired
    OnlineCacheRepository onlineCacheRepository;

    @Autowired
    FilesystemProperties filesystemConfig;

    @BeforeEach
    void setUp() throws IOException {
        createDirectories(filesystemConfig.getDownloadsPath());
        createDirectories(filesystemConfig.getMoviesPath());
        createDirectories(filesystemConfig.getTvPath());
    }

    @Test
    @DisplayName("if media has year in name, media is moved to movies")
    void automoveMovie() throws IOException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        String name = "Beetlejuice";
        String video = "video.mp4";
        String title = format("%s (%s)", name, now.format(ISO_LOCAL_DATE));
        DownloadedMedia media = createMedia(name + "." + year, video, 6L);

        saveToOnlineCache(name, year, name, now.toInstant(UTC), MediaFileType.MOVIE);

        service.autoMoveMedia();

        assertTrue(Files.exists(
                fileService.toLocalPath(filesystemConfig.getMoviesPath(), title, title + ".mp4").path()));
        DownloadedMedia savedMedia = verifyDownloadedMedia(media);
        verifyAutoMovedMedia(savedMedia);
    }

    @Test
    @DisplayName("if media does not has year in name, media is moved to tv shows")
    void automoveTvShow() throws IOException, InterruptedException {
        String name = "Beetlejuice";
        String video = "video.mp4";
        DownloadedMedia media = createMedia(name, video, 6L);

        saveToOnlineCache(name, null, name, null, MediaFileType.TV);

        service.autoMoveMedia();

        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.getTvPath(), name, name + ".mp4").path()));
        DownloadedMedia savedMedia = verifyDownloadedMedia(media);
        verifyAutoMovedMedia(savedMedia);
    }

    @Test
    @DisplayName("media dir is cleaned if no other media in it")
    void automoveCleansDir() throws IOException, InterruptedException {
        String name = "SupermanZZZ";
        String video = "video.mp4";
        createMedia(name, video, 6L);

        saveToOnlineCache(name, null, name, null, MediaFileType.MOVIE);

        service.autoMoveMedia();

        assertFalse(Files.exists(fileService.toLocalPath(filesystemConfig.getDownloadsPath(), name).path()));
    }

    @Test
    @DisplayName("media dir is not cleaned if other media in it")
    void automoveDoesNotCleanDir() throws IOException, InterruptedException {
        String name = "Superman";
        String video = "video.mp4";
        String video2 = "video2.mp4";
        createMedia(name, video, 6L);
        createFile(6, filesystemConfig.getDownloadsPath() + "/" + name + "/" + video2);

        service.autoMoveMedia();

        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.getDownloadsPath(), name).path()));
    }

    @Test
    @DisplayName("sorting options found before move should choose by similarity first, then yearBiasedMovie")
    void sortingIsDoneCorrectly() throws IOException, InterruptedException {
        int searchYear = 2001;
        String searchName = "Lord Of The Rings";
        LocalDateTime ldt = LocalDateTime.of(searchYear, 1, 1, 1, 1);

        String name = "Lord of the Rings.2001";
        String video = "video.mp4";
        createMedia(name, video, 6L);

        saveToOnlineCache(searchName, searchYear, searchName, null, MediaFileType.TV);

        String unsimilarName = searchName + "a";
        saveToOnlineCache(searchName, searchYear, unsimilarName, ldt.toInstant(UTC), MediaFileType.MOVIE);

        service.autoMoveMedia();

        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.getTvPath(), searchName, searchName + ".mp4").path()));
        assertFalse(Files.exists(fileService.toLocalPath(filesystemConfig.getMoviesPath(), format("%s (%s)", unsimilarName, ldt.format(ISO_LOCAL_DATE)), video).path()));
    }

    private OnlineCacheItem saveToOnlineCache(String searchName, Integer searchYear, String title, Instant date, MediaFileType type) {
        OnlineCacheItem cacheItem = new OnlineCacheItem();
        cacheItem.setSearchName(searchName);
        cacheItem.setSearchYear(searchYear);
        cacheItem.setTitle(title);
        cacheItem.setDate(date);
        cacheItem.setMediaType(type);
        return onlineCacheRepository.save(cacheItem);
    }

    private void verifyAutoMovedMedia(DownloadedMedia savedMedia) {
        AutoMoveMedia autoMoveMedia = new AutoMoveMedia();
        autoMoveMedia.setFileName(savedMedia.getFileName());
        autoMoveMedia.setSimilarityPercent(100);
        Example<AutoMoveMedia> example = Example.of(autoMoveMedia);
        AutoMoveMedia foundAutoMoveMedia = autoMoveMediaRepository.findOne(example).get();
        assertNotNull(foundAutoMoveMedia.getId());
        assertEquals(foundAutoMoveMedia.getFileName(), savedMedia.getFileName());
    }

    private @NotNull DownloadedMedia verifyDownloadedMedia(DownloadedMedia media) {
        DownloadedMedia savedMedia = downloadedMediaRepository.findById(media.getId()).get();
        assertTrue(savedMedia.isTriedAutoMove());
        return savedMedia;
    }

    private DownloadedMedia createMedia(String initialName, String videoFile, long size) throws IOException {
        createFile((int) size, filesystemConfig.getDownloadsPath() + "/" + initialName + "/" + videoFile);

        DownloadedMedia downloadedMedia = new DownloadedMedia();
        downloadedMedia.setFileName(initialName + "/" + videoFile);
        downloadedMedia.setFileSize(size);
        downloadedMedia.setDateDownloaded(Instant.now(Clock.systemUTC()));
        downloadedMedia.setDownloadComplete(true);
        return downloadedMediaRepository.save(downloadedMedia);
    }
}