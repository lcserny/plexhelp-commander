package net.cserny.rename;

import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.rename.NameNormalizer.NameYear;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.cserny.filesystem.LocalFileService.WalkOptions.ONLY_DIRECTORIES;
import static net.cserny.rename.MediaDescription.generateDescFrom;

@Order(0)
@Component
@Slf4j
public class DiskSearcher implements Searcher {

    @Autowired
    FilesystemConfig filesystemConfig;

    @Autowired
    RenameConfig renameConfig;

    @Autowired
    LocalFileService localFileService;

    private final Pattern releaseDateRegex = Pattern.compile("\s+\\(\\d{4}(-\\d{2}-\\d{2})?\\)$");
    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        LocalPath mediaPath = localFileService.toLocalPath(switch (type) {
            case MOVIE -> filesystemConfig.getMoviesPath();
            case TV -> filesystemConfig.getTvPath();
        });

        List<String> nameVariants = new ArrayList<>();

        try {
            List<Path> paths = localFileService.walk(mediaPath, renameConfig.getMaxDepth(), ONLY_DIRECTORIES);

           nameVariants = paths.stream()
                    .filter(path -> !mediaPath.path().equals(path))
                    .map(this::convertAndtrimReleaseDate)
                    .map(diskPath -> parseDistance(diskPath, nameYear.name()))
                    .filter(diskPath -> excludeNotSimilarPaths(diskPath, nameYear.name()))
                    .sorted(Comparator.comparing(DiskPath::similarity))
                    .map(this::chooseCorrectPath)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("Could not walk path " + mediaPath.path(), e);
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
        if (simPercent >= renameConfig.getSimilarityPercent()) {
            log.info("For path {}, the disk path {} is {}% similar with distance of {}",
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
