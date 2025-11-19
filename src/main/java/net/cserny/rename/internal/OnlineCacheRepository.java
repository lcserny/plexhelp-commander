package net.cserny.rename.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.MediaFileType;
import net.cserny.rename.NameNormalizer.NameYear;
import net.cserny.rename.OnlineCacheItem;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Repository
public class OnlineCacheRepository {

    private final InternalOnlineCacheRepository internalOnlineCacheRepository;

    public List<OnlineCacheItem> findByNameTypeAndOptionalYear(NameYear nameYear, MediaFileType type) {
        return findByNameTypeAndOptionalYear(nameYear.name(), nameYear.year(), type);
    }

    public List<OnlineCacheItem> findByNameTypeAndOptionalYear(String name, Integer year, MediaFileType type) {
        if (year == null) {
            return internalOnlineCacheRepository.findByNameAndType(name, type);
        }
        return internalOnlineCacheRepository.findByNameYearAndType(name, year, type);
    }

    public List<OnlineCacheItem> saveAll(Collection<OnlineCacheItem> items) {
        return items.stream().map(this::save).toList();
    }

    public OnlineCacheItem save(OnlineCacheItem item) {
        List<OnlineCacheItem> existing = findByNameTypeAndOptionalYear(item.getSearchName(), item.getSearchYear(), item.getMediaType());
        if (existing.isEmpty()) {
            return internalOnlineCacheRepository.save(item);
        }

        boolean existingEqual = existing.stream().anyMatch(existingItem -> existingItem.equals(item));
        if (!existingEqual) {
            return internalOnlineCacheRepository.save(item);
        }

        return existing.getFirst();
    }
}
