package net.cserny.rename;

import net.cserny.rename.NameNormalizer.NameYear;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Priority(1)
@Singleton
public class OnlineCacheSearcher implements Searcher {

    @Inject
    OnlineCacheRepository repository;

    @Override
    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        List<OnlineCacheItem> items = repository.retrieveAllByNameYearAndType(nameYear, type);

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
