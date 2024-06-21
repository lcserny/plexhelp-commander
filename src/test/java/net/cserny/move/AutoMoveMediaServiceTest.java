package net.cserny.move;

import net.cserny.AbstractInMemoryFileService;
import net.cserny.MongoTestConfiguration;
import net.cserny.TestConfig;
import net.cserny.download.DownloadedMedia;
import net.cserny.download.DownloadedMediaRepository;
import net.cserny.filesystem.FilesystemProperties;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Instant;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        TestConfig.class,
})
@DataMongoTest
@Testcontainers
class AutoMoveMediaServiceTest extends AbstractInMemoryFileService {

    @Autowired
    AutoMoveMediaService service;

    @Autowired
    DownloadedMediaRepository downloadedMediaRepository;

    @Autowired
    AutoMoveMediaRepository autoMoveMediaRepository;

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
        int year = 1988;
        String name = "Beetlejuice";
        String video = "video.mp4";
        DownloadedMedia media = createMedia(name + "." + year, video, 6L);

        service.autoMoveMedia();

        assertTrue(Files.exists(
                fileService.toLocalPath(filesystemConfig.getMoviesPath(), format("%s (%d)", name, year), video).path()));
        DownloadedMedia savedMedia = verifyDownloadedMedia(media);
        verifyAutoMovedMedia(savedMedia);
    }

    @Test
    @DisplayName("if media does not has year in name, media is moved to tv shows")
    void automoveTvShow() throws IOException, InterruptedException {
        String name = "Beetlejuice";
        String video = "video.mp4";
        DownloadedMedia media = createMedia(name, video, 6L);

        service.autoMoveMedia();

        assertTrue(Files.exists(fileService.toLocalPath(filesystemConfig.getTvPath(), name, video).path()));
        DownloadedMedia savedMedia = verifyDownloadedMedia(media);
        verifyAutoMovedMedia(savedMedia);
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
        createFile(filesystemConfig.getDownloadsPath() + "/" + initialName + "/" + videoFile, (int) size);

        DownloadedMedia downloadedMedia = new DownloadedMedia();
        downloadedMedia.setFileName(initialName + "/" + videoFile);
        downloadedMedia.setFileSize(size);
        downloadedMedia.setDateDownloaded(Instant.now(Clock.systemUTC()));
        return downloadedMediaRepository.save(downloadedMedia);
    }
}