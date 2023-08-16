package net.cserny.rename;

import net.cserny.rename.NameNormalizer.NameYear;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Order(1)
@Component
public class OnlineCacheSearcher implements Searcher {

    @Autowired
    OnlineCacheRepository repository;

    @Override
    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        List<OnlineCacheItem> items = repository.autoRetrieveAllByNameYearAndType(nameYear, type);

        List<MediaDescription> mediaDescriptions = items.stream()
                .map(this::convert)
                .collect(Collectors.toList());

        return new RenamedMediaOptions(MediaRenameOrigin.CACHE, mediaDescriptions);
    }

    private MediaDescription convert(OnlineCacheItem item) {
        return new MediaDescription(
                item.coverPath,
                item.title,
                item.date,
                item.description,
                item.cast
        );
    }
}
