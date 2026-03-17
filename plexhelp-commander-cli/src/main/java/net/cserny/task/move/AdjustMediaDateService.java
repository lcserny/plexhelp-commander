package net.cserny.task.move;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.LocalPathHandler;
import net.cserny.api.MediaIdentifier;
import net.cserny.api.WalkOptions;
import net.cserny.api.dto.LocalPath;
import net.cserny.config.FilesystemProperties;
import net.cserny.generated.MediaFileType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.cserny.support.UtilityProvider.toLoggableString;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdjustMediaDateService {

    private static final Pattern datePattern = Pattern.compile("(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})");
    private static final String separator = " -> ";
    private final FilesystemProperties filesystemProperties;
    private final LocalPathHandler localPathHandler;
    private final MediaIdentifier mediaIdentifier;

    public void adjustDate(String backupFilePath) throws IOException {
        Set<DateAdjustEntry> currentEntries = readCurrentEntries(backupFilePath);
        adjustDateInternal(filesystemProperties.getTvPath(), MediaFileType.TV, currentEntries);
        adjustDateInternal(filesystemProperties.getMoviesPath(), MediaFileType.MOVIE, currentEntries);
        writeCurrentEntries(currentEntries, backupFilePath);
    }

    private void adjustDateInternal(String rootMediaPath, MediaFileType type, Set<DateAdjustEntry> currentEntries) {
        LocalPath walkPath = localPathHandler.toLocalPath(rootMediaPath);
        List<LocalPath> foldersFound;
        try {
            foldersFound = localPathHandler.walk(walkPath, 2, WalkOptions.ONLY_DIRECTORIES);
        } catch (IOException e) {
            log.error("Error while walking folders {}", walkPath, e);
            return;
        }

        for (int i = 1; i < foldersFound.size(); i++) {
            LocalPath mediaFolder = foldersFound.get(i);

            List<LocalPath> filesInMediaFolder;
            try {
                filesInMediaFolder = localPathHandler.walk(mediaFolder, 4, WalkOptions.ONLY_FILES);
            } catch (IOException e) {
                log.error("Error while walking media folder {}", mediaFolder, e);
                continue;
            }

            List<LocalPath> mediaFilesInMediaFolder = new ArrayList<>();
            for (LocalPath file : filesInMediaFolder) {
                if (mediaIdentifier.isMedia(file)) {
                    mediaFilesInMediaFolder.add(file);
                }
            }

            List<LocalPath> subtitleFilesInMediaFolder = new ArrayList<>();
            for (LocalPath file : filesInMediaFolder) {
                if (mediaIdentifier.isSubtitle(file)) {
                    subtitleFilesInMediaFolder.add(file);
                }
            }

            try {
                if (!mediaFilesInMediaFolder.isEmpty()) {
                    if (type == MediaFileType.MOVIE && mediaFilesInMediaFolder.size() == 1) {
                        processMovieMediaEntry(mediaFilesInMediaFolder.getFirst().path(), currentEntries);
                    } else {
                        mediaFilesInMediaFolder.forEach(mediaFile -> processMediaEntry(mediaFile.path(), currentEntries));
                    }

                    subtitleFilesInMediaFolder.forEach(subtitleFile -> processMediaEntry(subtitleFile.path(), currentEntries));

                    // folder will be renamed after the files
                    processMediaEntry(mediaFolder.path(), currentEntries);
                }
            } catch (Exception e) {
                log.error("Error while processing media folder {}", mediaFolder, e);
            }
        }
    }

    private String getNewMediaName(String originalMediaName) {
        Matcher matcher = datePattern.matcher(originalMediaName);
        if (!matcher.find()) {
            return originalMediaName;
        }
        String year = matcher.group("year");
        return originalMediaName.replaceAll(datePattern.pattern(), year);
    }

    private void processMovieMediaEntry(Path originalPath, Set<DateAdjustEntry> entries) {
        String newMediaName = getNewMediaName(originalPath.getParent().getFileName().toString());
        String originalName = originalPath.getFileName().toString();
        String extension = originalName.substring(originalName.lastIndexOf('.'));
        String newName = newMediaName + extension;
        Path newPath = originalPath.getParent().resolve(newName);
        saveAndMoveEntry(originalPath.toString(), newPath.toString(), entries);
    }

    private void processMediaEntry(Path originalPath, Set<DateAdjustEntry> entries) {
        String newMediaName = getNewMediaName(originalPath.getFileName().toString());
        Path newPath = originalPath.getParent().resolve(newMediaName);
        saveAndMoveEntry(originalPath.toString(), newPath.toString(), entries);
    }

    @SneakyThrows
    private void saveAndMoveEntry(String source, String target, Set<DateAdjustEntry> entries) {
        if (source.equals(target)) {
            log.debug("Skipping rename of {} since its the same as the target location", source);
            return;
        }

        log.info("Renaming from {} to {}", source, target);
        localPathHandler.rename(localPathHandler.toLocalPath(source), localPathHandler.toLocalPath(target));

        entries.add(new DateAdjustEntry(source, target));
    }

    private Set<DateAdjustEntry> readCurrentEntries(String backupFilePathStr) throws IOException {
        Path backupFilePath = Paths.get(backupFilePathStr);
        if (!Files.exists(backupFilePath)) {
            return new HashSet<>();
        }

        Predicate<String[]> chunksFilter = chunks -> chunks.length == 2;
        try (Stream<String> lines = Files.lines(backupFilePath)) {
            return lines.map(l -> l.split(separator))
                    .peek(chunks -> {
                        if (!chunksFilter.test(chunks)) {
                            log.warn("Line split resulted in unexpected chunks {}", toLoggableString(chunks));
                        }
                    })
                    .filter(chunksFilter)
                    .map(chunks -> new DateAdjustEntry(chunks[0], chunks[1]))
                    .collect(Collectors.toSet());
        }
    }

    private void writeCurrentEntries(Set<DateAdjustEntry> currentEntries, String backupFilePath) throws IOException {
        List<String> lines = currentEntries.stream()
                .map(e -> String.format("%s%s%s", e.source(), separator, e.target()))
                .toList();
        Files.write(Paths.get(backupFilePath), lines);
    }

    record DateAdjustEntry(String source, String target) {}
}
