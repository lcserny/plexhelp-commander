package net.cserny.move;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.filesystem.LocalFileService;
import net.cserny.filesystem.LocalPath;
import net.cserny.generated.MediaFileGroup;
import net.cserny.generated.MediaFileType;
import net.cserny.rename.TVDataExtractor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.cserny.support.UtilityProvider.toLoggableString;

@Slf4j
@RequiredArgsConstructor
@Component
public class MediaGrouper {

    private static final List<Locale> relevantLocales = List.of(Locale.ENGLISH, Locale.of("ro"));

    private final LocalFileService fileService;

    private Map<Pattern, String> localePatterns;

    @PostConstruct
    public void init() {
        this.localePatterns = relevantLocales.stream().collect(Collectors.toMap(
                locale -> {
                    String lang = Pattern.quote(locale.getLanguage().toLowerCase());
                    String display = Pattern.quote(locale.getDisplayLanguage().toLowerCase());
                    String regex = "(?i)[. ](" + lang + "|" + display + ")[. ]";
                    return Pattern.compile(regex);
                },
                Locale::getLanguage,
                (existing, replacement) -> existing
        ));
    }

    public GroupedVideos separateDeletable(MediaFileGroup fileGroup, MediaFileType type) {
        List<String> videos = new ArrayList<>(fileGroup.getVideos());
        List<LocalPath> deletableVideos = new ArrayList<>();

        if (type == MediaFileType.MOVIE && videos.size() > 1) {
            log.info("Movie has a large sample file also, processing it out {}", fileGroup.getName());
            updateVideosForLargeSampleFile(videos, deletableVideos, fileGroup.getPath());
        }

        return new GroupedVideos(videos, deletableVideos);
    }

    public Map<LangKey, List<LocalPath>> subsByLang(List<LocalPath> subs) {
        return subs.stream()
                .collect(Collectors.groupingBy(this::detectLanguage, LinkedHashMap::new, Collectors.toList()));
    }

    private LangKey detectLanguage(LocalPath sub) {
        String path = sub.path().toAbsolutePath().toString();
        Integer season = TVDataExtractor.findSeason(path);
        int seasonToUse = season != null ? season : 0;

        for (Map.Entry<Pattern, String> entry : localePatterns.entrySet()) {
            if (entry.getKey().matcher(path).find()) {
                return new LangKey(entry.getValue(), seasonToUse);
            }
        }
        return new LangKey(seasonToUse);
    }

    private void updateVideosForLargeSampleFile(List<String> videos, List<LocalPath> deletableVideos, String fileGroupPath) {
        List<Pair<String, LocalPath>> list = videos.stream().parallel()
                .map(s -> {
                    LocalPath localPath = fileService.toLocalPath(fileGroupPath, s);
                    return Pair.of(s, localPath);
                })
                .sorted(Comparator.comparingLong(pair -> pair.getRight().attributes().size()))
                .collect(Collectors.toCollection(ArrayList::new));

        videos.clear();
        videos.add(list.removeLast().getLeft());

        list.forEach(pair -> deletableVideos.add(pair.getRight()));
    }

    public record LangKey(String iso2Code, int season) {

        public static final String NO_LANG = "___";

        public LangKey(String iso2Code, int season) {
            this.iso2Code = iso2Code;
            this.season = season;
        }

        public LangKey(int season) {
            this(NO_LANG, season);
        }
    }

    public record GroupedVideos(List<String> videos, List<LocalPath> deletableVideos) { }
}
