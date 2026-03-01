package net.cserny.task.subs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.CommandExecutingService;
import net.cserny.api.LocalPathHandler;
import net.cserny.api.MediaIdentifier;
import net.cserny.api.WalkOptions;
import net.cserny.api.dto.CommandResult;
import net.cserny.api.dto.SubtitleStreams;
import net.cserny.api.dto.LocalPath;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.cserny.api.Command.CommandName.REDUCE_SUBS;
import static net.cserny.api.Command.CommandName.SCAN_SUBS;
import static net.cserny.config.ApplicationConfig.MAX_SUBS_ALLOWED;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReduceSubtitlesService {

    private final MediaIdentifier mediaIdentifier;
    private final LocalPathHandler localPathHandler;
    private final CommandExecutingService localCommandService;
    private final SubtitleReducedMediaRepository subtitleReducedMediaRepository;

    public void run(String path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Path needs to be provided but was null");
        }

        LocalPath walkPath = localPathHandler.toLocalPath(path);
        List<LocalPath> filesFound = localPathHandler.walk(walkPath, 4, WalkOptions.ONLY_FILES);

        filesFound.forEach(localPath -> {
            if (!mediaIdentifier.isMedia(localPath)) {
                return;
            }

            Optional<SubtitleReducedMedia> reducedMediaOptional = subtitleReducedMediaRepository.findByFilePath(localPath.path().toString());
            if (reducedMediaOptional.isPresent() && reducedMediaOptional.get().isReduced()) {
                log.info("Media file was already stripped of unneeded subtitles {}", localPath.path());
                return;
            }

            Optional<CommandResult<SubtitleStreams>> scanResultOptional = localCommandService.execute(SCAN_SUBS, new String[]{localPath.path().toString()});
            if (scanResultOptional.isEmpty()) {
                return;
            }

            CommandResult<SubtitleStreams> scanResult = scanResultOptional.get();
            if (!scanResult.success()) {
                throw new RuntimeException("Failed to scan for subtitles for " + localPath.path());
            }

            SubtitleStreams subtitleStreams = scanResult.result();
            if (subtitleStreams.totalStreams() <= MAX_SUBS_ALLOWED) {
                return;
            }

            List<Integer> subtitleIndexes = subtitleStreams.engIndexes();
            String subtitleIndexesString = subtitleIndexes.stream().map(String::valueOf).collect(Collectors.joining(","));
            Optional<CommandResult<String>> reduceResultOptional =
                    localCommandService.execute(REDUCE_SUBS, new String[]{localPath.path().toString(), localPath.path().toString(), subtitleIndexesString});
            if (reduceResultOptional.isEmpty()) {
                return;
            }

            CommandResult<String> reduceResult = reduceResultOptional.get();
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
