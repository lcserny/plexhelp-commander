package net.cserny.task.move;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.LocalPathHandler;
import net.cserny.api.MediaIdentifier;
import net.cserny.api.WalkOptions;
import net.cserny.api.dto.LocalPath;
import net.cserny.config.FilesystemProperties;
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
//    private static final Pattern langDataPattern = Pattern.compile("(\\.[a-zA-Z]{2})?(\\.\\(\\d\\))?");
    private static final String separator = " -> ";
    private final FilesystemProperties filesystemProperties;
    private final LocalPathHandler localPathHandler;
    private final MediaIdentifier mediaIdentifier;

    public void adjustDate(String backupFilePath) throws IOException {
        Set<DateAdjustEntry> currentEntries = readCurrentEntries(backupFilePath);

        List.of(filesystemProperties.getTvPath(), filesystemProperties.getMoviesPath())
                .forEach(path -> adjustDate(path, currentEntries));

        writeCurrentEntries(currentEntries, backupFilePath);
    }

    private void adjustDate(String rootMediaPath, Set<DateAdjustEntry> currentEntries) {
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

//            List<LocalPath> subtitleFilesInMediaFolder = new ArrayList<>();
//            for (LocalPath file : filesInMediaFolder) {
//                if (mediaIdentifier.isSubtitle(file)) {
//                    subtitleFilesInMediaFolder.add(file);
//                }
//            }

            if (!mediaFilesInMediaFolder.isEmpty()) {
                String overrideName = processMediaFolder(mediaFolder.path(), currentEntries);
                processMediaFiles(overrideName, mediaFilesInMediaFolder, currentEntries);
//                processSubtitleFiles(overrideName, subtitleFilesInMediaFolder, currentEntries);
            }
        }
    }

    @SneakyThrows
    private String processMediaFolder(Path originalPath, Set<DateAdjustEntry> entries) {
        String originalName = originalPath.getFileName().toString();
        Matcher matcher = datePattern.matcher(originalName);

        if (matcher.find()) {
            String year = matcher.group("year");
            String newName = originalName.replaceAll(datePattern.pattern(), year);
            Path newPath = originalPath.getParent().resolve(newName);

            DateAdjustEntry entry = new DateAdjustEntry(originalPath.toString(), newPath.toString());
            entries.add(entry);

            log.info("Moving from {} to {}", entry.source(), entry.target());
//            localPathHandler.rename(localPathHandler.toLocalPath(entry.source()), localPathHandler.toLocalPath(entry.target()));

            return newName;
        }

        return originalName;
    }

    @SneakyThrows
    private void processMediaFiles(String mediaFolder, List<LocalPath> mediaFiles, Set<DateAdjustEntry> entries) {
        for (LocalPath originalMediaFilePath : mediaFiles) {
            String originalName = originalMediaFilePath.path().getFileName().toString();
            String extension = originalName.substring(originalName.lastIndexOf('.'));

            // TODO not ok for tv shows...
            String newName = mediaFolder + extension;
            Path newPath = originalMediaFilePath.path().getParent().resolve(newName);

            DateAdjustEntry entry = new DateAdjustEntry(originalMediaFilePath.toString(), newPath.toString());
            entries.add(entry);

            log.info("Moving from {} to {}", entry.source(), entry.target());
//            localPathHandler.rename(localPathHandler.toLocalPath(entry.source()), localPathHandler.toLocalPath(entry.target()));
        }
    }

    @SneakyThrows
    private void processSubtitleFiles(String mediaFolder, List<LocalPath> subtitleFiles, Set<DateAdjustEntry> entries) {
        if (subtitleFiles.size() == 1) {
            LocalPath originalSubtitleFilePath = subtitleFiles.getFirst();
            String originalName = originalSubtitleFilePath.path().getFileName().toString();
            String extension = originalName.substring(originalName.lastIndexOf('.'));

//            boolean hasLangData = false;
//            String langData = originalName.substring(originalName.indexOf("."));
//            if (!langData.equals(extension)) {
//                langData = langData.replace(extension, "");
//                if (langDataPattern.matcher(langData).matches()) {
//                    hasLangData = true;
//                }
//            }

//            String newName = mediaFolder + (hasLangData ? langData : "") + extension;
            String newName = mediaFolder + extension;
            Path newPath = originalSubtitleFilePath.path().getParent().resolve(newName);

            DateAdjustEntry entry = new DateAdjustEntry(originalSubtitleFilePath.toString(), newPath.toString());
            entries.add(entry);

            log.info("Moving from {} to {}", entry.source(), entry.target());
//            localPathHandler.rename(localPathHandler.toLocalPath(entry.source()), localPathHandler.toLocalPath(entry.target()));
        }

/*        for (LocalPath originalSubtitleFilePath : subtitleFiles) {
            String originalName = originalSubtitleFilePath.path().getFileName().toString();
            String extension = originalName.substring(originalName.lastIndexOf('.'));

            boolean hasLangData = false;
            String langData = originalName.substring(originalName.indexOf("."));
            if (!langData.equals(extension)) {
                langData = langData.replace(extension, "");
                if (langDataPattern.matcher(langData).matches()) {
                    hasLangData = true;
                }
            }

            String newName = mediaFolder + (hasLangData ? langData : "") + extension;
            Path newPath = originalSubtitleFilePath.path().getParent().resolve(newName);

            DateAdjustEntry entry = new DateAdjustEntry(originalSubtitleFilePath.toString(), newPath.toString());
            entries.add(entry);

            log.info("Moving from {} to {}", entry.source(), entry.target());
            localPathHandler.rename(localPathHandler.toLocalPath(entry.source()), localPathHandler.toLocalPath(entry.target()));
        }*/
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
