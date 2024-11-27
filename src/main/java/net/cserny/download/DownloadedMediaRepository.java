package net.cserny.download;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class DownloadedMediaRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    public Optional<DownloadedMedia> findById(ObjectId id) {
        return Optional.ofNullable(mongoTemplate.findById(id, DownloadedMedia.class));
    }

    public List<DownloadedMedia> findAllWith(LocalDate date, Boolean downloadComplete, List<String> names) {
        Query query = new Query();

        if (date != null) {
            Instant startDate = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant endDate = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            query.addCriteria(Criteria.where("dateDownloaded").gte(startDate).lt(endDate));
        }

        if (downloadComplete != null) {
            query.addCriteria(Criteria.where("downloadComplete").is(downloadComplete));
        }

        if (names != null && !names.isEmpty()) {
            query.addCriteria(Criteria.where("fileName").in(names));
        }

        return mongoTemplate.find(query, DownloadedMedia.class);
    }

    public List<DownloadedMedia> findForAutoMove(Boolean triedAutoMove, int limit) {
        Query query = new Query();

        if (triedAutoMove != null) {
            query.addCriteria(Criteria.where("triedAutoMove").is(triedAutoMove));
        }

        query.with(Sort.by(Sort.Direction.DESC, "dateDownloaded"));
        query.limit(limit);

        return mongoTemplate.find(query, DownloadedMedia.class);
    }

    public DownloadedMedia save(DownloadedMedia media) {
        return mongoTemplate.save(media);
    }

    public List<DownloadedMedia> saveAll(List<DownloadedMedia> medias) {
        return medias.stream().map(this::save).toList();
    }
}
