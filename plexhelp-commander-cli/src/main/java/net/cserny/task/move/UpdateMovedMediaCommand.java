package net.cserny.task.move;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.AbstractCliCommand;
import net.cserny.config.FilesystemProperties;
import net.cserny.generated.MediaFileType;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Slf4j
@RequiredArgsConstructor
@Component
@Command(name = "update-moved", description = "Updates movedMedia collection by retrieving fresh TMDB details for media.")
public class UpdateMovedMediaCommand extends AbstractCliCommand {

    private final FilesystemProperties filesystemProperties;
    private final UpdateMovedMediaService updateMovedMediaService;

    @Override
    protected void run() throws Exception {
        updateMovedMediaService.updateMovedMedia(filesystemProperties.getTvPath(), MediaFileType.TV);
        updateMovedMediaService.updateMovedMedia(filesystemProperties.getMoviesPath(), MediaFileType.MOVIE);
    }
}
