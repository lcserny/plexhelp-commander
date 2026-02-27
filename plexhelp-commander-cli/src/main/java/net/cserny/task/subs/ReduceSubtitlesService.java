package net.cserny.task.subs;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.LocalPathHandler;
import net.cserny.api.MediaIdentifier;
import net.cserny.config.FilesystemProperties;
import net.cserny.core.command.Command;
import net.cserny.core.command.LocalCommandService;
import net.cserny.core.command.ffmpeg.FfmpegReduceSubtitles;
import net.cserny.core.command.ffmpeg.FfmpegScanStreams;
import net.cserny.fs.LocalPath;
import net.cserny.task.TaskRunner;
import net.cserny.task.TaskType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReduceSubtitlesService implements TaskRunner {

    private final MediaIdentifier mediaIdentifier;
    private final LocalPathHandler localPathHandler;
    private final FilesystemProperties filesystemProperties;
    private final LocalCommandService localCommandService;
    private final SubtitleReducedMediaRepository  subtitleReducedMediaRepository;

    @Override
    public boolean supports(TaskType taskType) {
        return taskType == TaskType.reduce_subs;
    }

    @SneakyThrows
    @Override
    public void run() {
        LocalPath walkPath = localPathHandler.toLocalPath(filesystemProperties.getTvPath());
        List<LocalPath> filesFound = localPathHandler.walk(walkPath, 4);

        filesFound.forEach(localPath -> {
            if (!mediaIdentifier.isMedia(localPath)) {
                return;
            }

            Optional<SubtitleReducedMedia> reducedMediaOptional = subtitleReducedMediaRepository.findByFilePath(localPath.path().toString());
            if (reducedMediaOptional.isPresent() && reducedMediaOptional.get().isReduced()) {
                log.info("Media file was already stripped of unneeded subtitles {}", localPath.path());
                return;
            }

            Optional<Command.CommandResult<List<Integer>>> scanResultOptional = localCommandService.execute(FfmpegScanStreams.NAME, new String[]{localPath.path().toString()});
            if (scanResultOptional.isEmpty()) {
                return;
            }

            Command.CommandResult<List<Integer>> scanResult = scanResultOptional.get();
            if (!scanResult.success()) {
                throw new RuntimeException("Failed to scan for subtitles for " + localPath.path());
            }

            if (scanResult.result().isEmpty()) {
                return;
            }

            List<Integer> subtitleIndexes = scanResult.result();
            String subtitleIndexesString = subtitleIndexes.stream().map(String::valueOf).collect(Collectors.joining(","));
            Optional<Command.CommandResult<String>> reduceResultOptional = localCommandService.execute(FfmpegReduceSubtitles.NAME, new String[]{localPath.path().toString(), subtitleIndexesString});
            if (reduceResultOptional.isEmpty()) {
                return;
            }

            Command.CommandResult<String> reduceResult = reduceResultOptional.get();
            if (!reduceResult.success()) {
                throw new RuntimeException("Failed to reduce subtitles for " + localPath.path());
            }

            subtitleReducedMediaRepository.save(SubtitleReducedMedia.builder()
                    .filePath(localPath.path().toString())
                    .reduced(true)
                    .build());
            log.info("Persisted subtitle reduced media {}",  localPath.path());
        });
    }
}
