package net.cserny.rename;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.MediaDescriptionData;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaRenameOrigin;
import net.cserny.generated.RenamedMediaOptions;
import net.cserny.rename.NameNormalizer.NameYear;
import net.cserny.rename.TmdbWrapper.Credits;
import net.cserny.rename.TmdbWrapper.Movie;
import net.cserny.rename.TmdbWrapper.Person;
import net.cserny.rename.TmdbWrapper.Tv;
import net.cserny.rename.internal.OnlineCacheRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.cserny.support.UtilityProvider.toOneLineString;

@Order(2)
@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalSearcher implements Searcher {

    private final OnlineCacheRepository repository;
    private final OnlineProperties onlineConfig;
    private final TmdbWrapper tmdbWrapper;

    private final Pattern specialCharsRegex = Pattern.compile("[^a-zA-Z0-9-\s]");

    @Override
    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        List<MediaDescriptionData> mediaFound = switch (type) {
            case MOVIE -> searchMovie(nameYear);
            case TV -> searchTvShow(nameYear);
        };

        Set<OnlineCacheItem> items = convertAll(nameYear, mediaFound, type);
        log.info("Saving media found to cache {}", toOneLineString(items));
        repository.saveAll(items);

        return new RenamedMediaOptions().origin(MediaRenameOrigin.EXTERNAL).mediaDescriptions(mediaFound);
    }

    private OnlineCacheItem convert(NameYear nameYear, MediaDescriptionData description, MediaFileType mediaType) {
        OnlineCacheItem item = new OnlineCacheItem();
        item.setSearchName(nameYear.name());
        item.setSearchYear(nameYear.year());
        item.setCoverPath(description.getPosterUrl());
        item.setTitle(description.getTitle());
        item.setDate(StringUtils.isBlank(description.getDate()) ? null : LocalDate.parse(description.getDate()).atStartOfDay(ZoneOffset.UTC).toInstant());
        item.setDescription(description.getDescription());
        item.setCast(description.getCast());
        item.setMediaType(mediaType);
        return item;
    }

    private Set<OnlineCacheItem> convertAll(NameYear nameYear, List<MediaDescriptionData> descriptions, MediaFileType mediaType) {
        return descriptions.stream()
                .map(description -> this.convert(nameYear, description, mediaType))
                .collect(Collectors.toSet());
    }

    private List<MediaDescriptionData> searchTvShow(NameYear nameYear) {
        List<Tv> results = tmdbWrapper.searchTvShows(nameYear.name(), nameYear.year());
        if (results.isEmpty()) {
            log.info("No TV show results found");
            return Collections.emptyList();
        }

        List<Tv> sublist = results.subList(0, Math.min(results.size(), onlineConfig.getResultLimit()));
        log.info("TV show results found {}", toOneLineString(sublist));

        List<MediaDescriptionData> descriptions = new ArrayList<>();
        for (Tv tvSeries : sublist) {
            String posterUrl = producePosterUrl(tvSeries.getPosterPath());
            String title = processTitle(tvSeries.getName());
            String date = tvSeries.getFirstAirDate();
            String description = nullIfBlank(tvSeries.getOverview());
            List<String> cast = produceCast(tmdbWrapper.tvShowCredits(tvSeries.getId()));

            descriptions.add(new MediaDescriptionData().posterUrl(posterUrl).title(title)
                    .date(date).description(description).cast(cast));
        }

        return descriptions;
    }

    private List<MediaDescriptionData> searchMovie(NameYear nameYear) {
        List<Movie> results = tmdbWrapper.searchMovies(nameYear.name(), nameYear.year());
        if (results.isEmpty()) {
            log.info("No movie results found");
            return Collections.emptyList();
        }

        List<Movie> sublist = results.subList(0, Math.min(results.size(), onlineConfig.getResultLimit()));
        log.info("Movie results found {}", toOneLineString(sublist));

        List<MediaDescriptionData> descriptions = new ArrayList<>();
        for (Movie movieDb : sublist) {
            String posterUrl = producePosterUrl(movieDb.getPosterPath());
            String title = processTitle(movieDb.getTitle());
            String date = movieDb.getReleaseDate();
            String description = nullIfBlank(movieDb.getOverview());
            List<String> cast = produceCast(tmdbWrapper.movieCredits(movieDb.getId()));

            descriptions.add(new MediaDescriptionData().posterUrl(posterUrl).title(title)
                    .date(date).description(description).cast(cast));
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
