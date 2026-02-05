package net.cserny.core.rename;

import lombok.extern.slf4j.Slf4j;
import net.cserny.config.RenameProperties;
import net.cserny.fs.FilesystemProperties;
import net.cserny.fs.LocalFileService;
import net.cserny.fs.LocalPath;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaRenameOrigin;
import net.cserny.generated.RenamedMediaOptions;
import net.cserny.core.rename.NameNormalizer.NameYear;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static net.cserny.support.UtilityProvider.toLoggableString;
import static net.cserny.fs.ExcludingFileVisitor.WalkOptions.ONLY_DIRECTORIES;
import static net.cserny.core.rename.MediaRenameService.generateDescDataFrom;

@Order(0)
@Component
@Slf4j
public class DiskSearcher implements Searcher {

    @Autowired
    FilesystemProperties filesystemConfig;

    @Autowired
    RenameProperties renameProperties;

    @Autowired
    LocalFileService localFileService;

    private final Pattern releaseDateRegex = Pattern.compile("\s+\\(\\d{4}(-\\d{2}-\\d{2})?\\)$");

    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        LocalPath mediaPath = localFileService.toLocalPath(switch (type) {
            case MOVIE -> filesystemConfig.getMoviesPath();
            case TV -> filesystemConfig.getTvPath();
        });

        List<String> nameVariants = new ArrayList<>();

        try {
            List<LocalPath> paths = localFileService.walk(mediaPath, renameProperties.getMaxDepth(), ONLY_DIRECTORIES);

           nameVariants = paths.stream().parallel()
                    .filter(path -> !mediaPath.equals(path))
                    .map(this::convertAndTrimReleaseDate)
                    .map(diskPath -> parseDistance(diskPath, nameYear.name()))
                    .filter(diskPath -> excludeNotSimilarPaths(diskPath, nameYear.name()))
                    .sorted(Comparator.comparing(DiskPath::distance))
                    .map(this::chooseCorrectPath)
                    .toList();
        } catch (IOException e) {
            log.warn("Could not walk path {}, error: {}", mediaPath, e.getMessage());
        }

        return new RenamedMediaOptions().origin(MediaRenameOrigin.DISK).mediaDescriptions(generateDescDataFrom(nameVariants));
    }

    private DiskPath convertAndTrimReleaseDate(LocalPath path) {
        String folder = path.path().getFileName().toString();
        String trimmedLocalPath = folder.replaceAll(releaseDateRegex.pattern(), "");
        return new DiskPath(null, folder, trimmedLocalPath);
    }

    private boolean excludeNotSimilarPaths(DiskPath diskPath, String name) {
        int bigger = Math.max(diskPath.trimmedLocalPpath().length(), name.length());
        int simPercent = SimilarityService.getSimilarityPercent(diskPath.distance(), bigger);
        if (simPercent >= renameProperties.getSimilarityPercent()) {
            log.info("For path {}, the disk path {} is {}% similar with distance of {}",
                    name, diskPath.trimmedLocalPpath(), simPercent, toLoggableString(diskPath.distance()));
            return true;
        }
        return false;
    }

    private DiskPath parseDistance(DiskPath diskPath, String name) {
        Integer distance = SimilarityService.getDistance(diskPath.trimmedLocalPpath(), name);
        return new DiskPath(distance, diskPath.localPath(), diskPath.trimmedLocalPpath());
    }

    private String chooseCorrectPath(DiskPath diskPath) {
        return diskPath.localPath();
    }

    private record DiskPath(Integer distance, String localPath, String trimmedLocalPpath) {
    }
}
