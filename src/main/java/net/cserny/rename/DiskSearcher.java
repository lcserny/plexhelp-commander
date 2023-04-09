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
        LocalPath mediaPath = localFileService.toLocalPath(switch (type) {
            case MOVIE -> filesystemConfig.moviesPath();
            case TV -> filesystemConfig.tvShowsPath();
        });

        List<String> nameVariants = new ArrayList<>();

        try {
            List<Path> paths = localFileService.walk(mediaPath, renameConfig.maxDepth(), ONLY_DIRECTORIES);

           nameVariants = paths.stream()
                    .filter(path -> !mediaPath.path().equals(path))
                    .map(this::convertAndtrimReleaseDate)
                    .map(diskPath -> parseDistance(diskPath, nameYear.name()))
                    .filter(diskPath -> excludeNotSimilarPaths(diskPath, nameYear.name()))
                    .sorted(Comparator.comparing(DiskPath::similarity))
                    .map(this::chooseCorrectPath)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.warn("Could not walk path " + mediaPath.path(), e);
        }

        return new RenamedMediaOptions(MediaRenameOrigin.DISK, generateDescFrom(nameVariants));
    }

    private DiskPath convertAndtrimReleaseDate(Path path) {
        String folder = path.getFileName().toString();
        String trimmedLocalPath = folder.replaceAll(releaseDateRegex.pattern(), "");
        return new DiskPath(null, folder, trimmedLocalPath);
    }

    private boolean excludeNotSimilarPaths(DiskPath diskPath, String name) {
        int bigger = Math.max(diskPath.trimmedLocalPpath().length(), name.length());
        int simPercent = (int)((float)(bigger - diskPath.similarity()) / (float)bigger * 100);
        if (simPercent >= renameConfig.similarityPercent()) {
            LOGGER.infov("For path {0}, the disk path {1} is {2}% similar with distance of {3}",
                    name, diskPath.trimmedLocalPpath(), simPercent, diskPath.similarity());
            return true;
        }
        return false;
    }

    private DiskPath parseDistance(DiskPath diskPath, String name) {
        Integer distance = levenshteinDistance.apply(diskPath.trimmedLocalPpath(), name);
        return new DiskPath(distance, diskPath.localPath(), diskPath.trimmedLocalPpath());
    }

    private String chooseCorrectPath(DiskPath diskPath) {
        if (diskPath.similarity() == 0) {
            return diskPath.localPath();
        } else {
            return diskPath.trimmedLocalPpath();
        }
    }

    private record DiskPath(Integer similarity, String localPath, String trimmedLocalPpath) {
    }
}
