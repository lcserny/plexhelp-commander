package net.cserny.rename;

import net.cserny.rename.NameNormalizer.NameYear;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Order(1)
@Component
public class OnlineCacheSearcher implements Searcher {

    public static final DateTimeFormatter utcDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    @Autowired
    OnlineCacheRepository repository;

    @Override
    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        List<OnlineCacheItem> items;
        if (nameYear.year() == null) {
            items = repository.findByNameAndType(nameYear.name(), type);
        } else {
            items = repository.findByNameYearAndType(nameYear.name(), nameYear.year(), type);
        }

        List<MediaDescription> mediaDescriptions = items.stream()
                .map(this::convert)
                .collect(Collectors.toList());

        return new RenamedMediaOptions(MediaRenameOrigin.CACHE, mediaDescriptions);
    }

    private MediaDescription convert(OnlineCacheItem item) {
        String date = item.getDate() == null ? null : utcDateFormatter.format(item.getDate());
        return new MediaDescription(
                item.getCoverPath(),
                item.getTitle(),
                date,
                item.getDescription(),
                item.getCast()
        );
    }
}
