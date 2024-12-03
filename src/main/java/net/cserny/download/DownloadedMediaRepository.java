package net.cserny.download;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    InternalDownloadedMediaRepository repository;

    public Optional<DownloadedMedia> findById(ObjectId id) {
        return this.repository.findById(id);
    }

    public DownloadedMedia save(DownloadedMedia media) {
        return this.repository.save(media);
    }

    public List<DownloadedMedia> saveAll(List<DownloadedMedia> medias) {
        return this.repository.saveAll(medias);
    }

    private Query buildFindAllQuery(LocalDate date, Boolean downloadComplete, List<String> names) {
        Query query = new Query();

        if (date != null) {
            Instant startDate = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant endDate = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            query.addCriteria(Criteria.where("dateDownloaded").gte(startDate).lt(endDate));
        }

        if (downloadComplete != null) {
            Criteria criteria = new Criteria().orOperator(
                    Criteria.where("downloadComplete").is(downloadComplete),
                    Criteria.where("downloadComplete").exists(false)
            );
            query.addCriteria(criteria);
        }

        if (names != null && !names.isEmpty()) {
            query.addCriteria(names.size() == 1
                    ? Criteria.where("fileName").regex(".*" + names.getFirst() + ".*", "i")
                    : Criteria.where("fileName").in(names)
            );
        }

        return query;
    }

    public List<DownloadedMedia> findAllWith(LocalDate date, Boolean downloadComplete, List<String> names) {
        Query query = this.buildFindAllQuery(date, downloadComplete, names);
        return mongoTemplate.find(query, DownloadedMedia.class);
    }

    public Page<DownloadedMedia> findAllPaginatedWith(LocalDate date, Boolean downloadComplete, List<String> names, Pageable pageable) {
        Query query = this.buildFindAllQuery(date, downloadComplete, names);
        query.skip(pageable.getOffset());
        query.limit(pageable.getPageSize());

        List<DownloadedMedia> results = mongoTemplate.find(query, DownloadedMedia.class);
        long total = mongoTemplate.count(query.skip(0).limit(0), DownloadedMedia.class);

        return new PageImpl<>(results, pageable, total);
    }

    public List<DownloadedMedia> findForAutoMove(int limit) {
        Query query = new Query();

        Criteria criteria = new Criteria().andOperator(
                new Criteria().orOperator(
                        Criteria.where("downloadComplete").is(true),
                        Criteria.where("downloadComplete").exists(false)
                ),
                new Criteria().orOperator(
                        Criteria.where("triedAutoMove").is(false),
                        Criteria.where("triedAutoMove").exists(false)
                )
        );
        query.addCriteria(criteria);

        query.with(Sort.by(Sort.Direction.DESC, "dateDownloaded"));
        query.limit(limit);

        return mongoTemplate.find(query, DownloadedMedia.class);
    }
}
