package net.cserny.rename;

import lombok.extern.slf4j.Slf4j;
import net.cserny.DataMapper;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaRenameOrigin;
import net.cserny.generated.RenamedMediaOptions;
import net.cserny.rename.NameNormalizer.NameYear;
import net.cserny.rename.TmdbWrapper.Credits;
import net.cserny.rename.TmdbWrapper.Movie;
import net.cserny.rename.TmdbWrapper.Person;
import net.cserny.rename.TmdbWrapper.Tv;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Order(2)
@Component
@Slf4j
public class ExternalSearcher implements Searcher {

    @Autowired
    OnlineCacheRepository repository;

    @Autowired
    OnlineProperties onlineConfig;

    @Autowired
    TmdbWrapper tmdbWrapper;

    private final Pattern specialCharsRegex = Pattern.compile("[^a-zA-Z0-9-\s]");

    @Override
    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        List<MediaDescription> mediaFound = switch (type) {
            case MOVIE -> searchMovie(nameYear);
            case TV -> searchTvShow(nameYear);
        };

        List<OnlineCacheItem> items = convertAll(nameYear, mediaFound, type);
        log.info("Saving media found to cache {}", items);
        repository.saveAll(items);

        return new RenamedMediaOptions().origin(MediaRenameOrigin.EXTERNAL)
                .mediaDescriptions(mediaFound.stream().map(DataMapper.INSTANCE::descriptionToDescriptionData).toList());
    }

    private OnlineCacheItem convert(NameYear nameYear, MediaDescription description, MediaFileType mediaType) {
        OnlineCacheItem item = new OnlineCacheItem();
        item.setSearchName(nameYear.name());
        item.setSearchYear(nameYear.year());
        item.setCoverPath(description.posterUrl());
        item.setTitle(description.title());
        item.setDate(StringUtils.isBlank(description.date()) ? null : LocalDate.parse(description.date()).atStartOfDay(ZoneOffset.UTC).toInstant());
        item.setDescription(description.description());
        item.setCast(description.cast());
        item.setMediaType(mediaType.getValue());
        return item;
    }

    private List<OnlineCacheItem> convertAll(NameYear nameYear, List<MediaDescription> descriptions, MediaFileType mediaType) {
        return descriptions.stream()
                .map(description -> this.convert(nameYear, description, mediaType))
                .collect(Collectors.toList());
    }

    private List<MediaDescription> searchTvShow(NameYear nameYear) {
        List<Tv> results = tmdbWrapper.searchTvShows(nameYear.name(), nameYear.year());
        if (results.isEmpty()) {
            log.info("No TV show results found");
            return Collections.emptyList();
        }

        List<Tv> sublist = results.subList(0, Math.min(results.size(), onlineConfig.getResultLimit()));
        log.info("TV show results found {}", sublist);

        List<MediaDescription> descriptions = new ArrayList<>();
        for (Tv tvSeries : sublist) {
            String posterUrl = producePosterUrl(tvSeries.getPosterPath());
            String title = processTitle(tvSeries.getName());
            String date = tvSeries.getFirstAirDate();
            String description = nullIfBlank(tvSeries.getOverview());
            List<String> cast = produceCast(tmdbWrapper.tvShowCredits(tvSeries.getId()));

            descriptions.add(new MediaDescription( posterUrl, title, date, description, cast ));
        }

        return descriptions;
    }

    private List<MediaDescription> searchMovie(NameYear nameYear) {
        List<Movie> results = tmdbWrapper.searchMovies(nameYear.name(), nameYear.year());
        if (results.isEmpty()) {
            log.info("No movie results found");
            return Collections.emptyList();
        }

        List<Movie> sublist = results.subList(0, Math.min(results.size(), onlineConfig.getResultLimit()));
        log.info("Movie results found {}", sublist);

        List<MediaDescription> descriptions = new ArrayList<>();
        for (Movie movieDb : sublist) {
            String posterUrl = producePosterUrl(movieDb.getPosterPath());
            String title = processTitle(movieDb.getTitle());
            String date = movieDb.getReleaseDate();
            String description = nullIfBlank(movieDb.getOverview());
            List<String> cast = produceCast(tmdbWrapper.movieCredits(movieDb.getId()));

            descriptions.add(new MediaDescription( posterUrl, title, date, description, cast ));
        }

        return descriptions;
    }

    private String processTitle(String title) {
        return title.replaceAll("&", "and")
                .replaceAll(specialCharsRegex.pattern(), "");
    }

    private String producePosterUrl(String posterPath) {
        return StringUtils.isBlank(posterPath) ? null : onlineConfig.getPosterBase() + posterPath;
    }

    private String nullIfBlank(String text) {
        return StringUtils.isBlank(text) ? null : text;
    }

    private List<String> produceCast(Credits credits) {
        if (credits == null) {
            return Collections.emptyList();
        }

        return credits.getCast().stream()
                .map(Person::getName)
                .limit(onlineConfig.getResultLimit())
                .toList();
    }
}
