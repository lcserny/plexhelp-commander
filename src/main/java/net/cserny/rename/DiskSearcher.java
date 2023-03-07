package net.cserny.rename;

import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.rename.NameNormalizer.NameYear;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jboss.logging.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.cserny.filesystem.LocalFileService.WalkOptions.ONLY_DIRECTORIES;
import static net.cserny.rename.MediaDescription.generateDescFrom;

@Priority(2)
@Singleton
public class DiskSearcher implements Searcher {

    private static final Logger LOGGER = Logger.getLogger(DiskSearcher.class);

    @Inject
    FilesystemConfig filesystemConfig;

    @Inject
    RenameConfig renameConfig;

    @Inject
    LocalFileService localFileService;

    private final Pattern releaseDateRegex = Pattern.compile("\s+\\(\\d{4}(-\\d{2}-\\d{2})?\\)$");
    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        LocalPath mediaPath = localFileService.produceLocalPath(switch (type) {
            case MOVIE -> filesystemConfig.moviesPath();
            case TV -> filesystemConfig.tvShowsPath();
        });

        List<String> nameVariants = new ArrayList<>();

        try {
            List<Path> paths = localFileService.walk(mediaPath, renameConfig.maxDepth(), ONLY_DIRECTORIES);

            nameVariants = paths.stream()
                    .filter(path -> !mediaPath.path().equals(path))
                    .map(this::convertAndtrimReleaseDate)
                    .map(folder -> parseDistance(folder, nameYear.name()))
                    .filter(pair -> excludeNotSimilarPaths(pair, nameYear.name()))
                    .sorted(Comparator.comparing(Pair::getLeft))
                    .map(Pair::getRight)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.warn("Could not walk path " + mediaPath.path(), e);
        }

        return new RenamedMediaOptions(MediaRenameOrigin.DISK, generateDescFrom(nameVariants));
    }

    private String convertAndtrimReleaseDate(Path path) {
        String folder = path.getFileName().toString();
        return folder.replaceAll(releaseDateRegex.pattern(), "");
    }

    private boolean excludeNotSimilarPaths(Pair<Integer, String> similarPath, String name) {
        int bigger = Math.max(similarPath.getRight().length(), name.length());
        int simPercent = (int)((float)(bigger - similarPath.getLeft()) / (float)bigger * 100);
        return simPercent >= renameConfig.similarityPercent();
    }

    private Pair<Integer, String> parseDistance(String folder, String name) {
        Integer distance = levenshteinDistance.apply(folder, name);
        return Pair.of(distance, folder);
    }
}
