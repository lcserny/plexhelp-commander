package net.cserny.rename;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.MediaDescriptionData;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaRenameOrigin;
import net.cserny.generated.RenamedMediaOptions;
import net.cserny.rename.NameNormalizer.NameYear;
import net.cserny.rename.internal.OnlineCacheRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Order(1)
@RequiredArgsConstructor
@Component
public class OnlineCacheSearcher implements Searcher {

    public static final DateTimeFormatter utcDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    private final OnlineCacheRepository repository;

    @Override
    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        List<OnlineCacheItem> items = repository.findByNameTypeAndOptionalYear(nameYear, type);
        List<MediaDescriptionData> mediaDescriptions = items.stream().map(this::convert).toList();

        return new RenamedMediaOptions().origin(MediaRenameOrigin.CACHE)
                .mediaDescriptions(mediaDescriptions);
    }

    private MediaDescriptionData convert(OnlineCacheItem item) {
        String date = item.getDate() == null ? null : utcDateFormatter.format(item.getDate());
        return new MediaDescriptionData().posterUrl(item.getCoverPath())
                .title(item.getTitle()).date(date).description(item.getDescription())
                .cast(item.getCast());
    }
}
