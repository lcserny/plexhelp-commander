package net.cserny.rename;

import lombok.extern.slf4j.Slf4j;
import net.cserny.DataMapper;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaRenameOrigin;
import net.cserny.generated.RenamedMediaOptions;
import net.cserny.rename.NameNormalizer.NameYear;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
            log.warn("No year specified, searching cache only by name {}", nameYear.name());
            items = repository.findByNameAndType(nameYear.name(), type.getValue());
        } else {
            items = repository.findByNameYearAndType(nameYear.name(), nameYear.year(), type.getValue());
        }

        List<MediaDescription> mediaDescriptions = items.stream()
                .map(this::convert)
                .collect(Collectors.toList());

        return new RenamedMediaOptions().origin(MediaRenameOrigin.CACHE)
                .mediaDescriptions(mediaDescriptions.stream().map(DataMapper.INSTANCE::descriptionToDescriptionData).toList());
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
