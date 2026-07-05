package net.cserny.core.rename;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.NameNormalizer.NameYear;
import net.cserny.api.RenameSearcher;
import net.cserny.config.OnlineProperties;
import net.cserny.core.rename.internal.OnlineCacheRepository;
import net.cserny.core.rename.tmdb.TmdbRestApi;
import net.cserny.core.rename.tmdb.TmdbRestApi.Movie;
import net.cserny.core.rename.tmdb.TmdbRestApi.MovieResults;
import net.cserny.core.rename.tmdb.TmdbRestApi.Tv;
import net.cserny.core.rename.tmdb.TmdbRestApi.TvResults;
import net.cserny.generated.MediaDescriptionData;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.MediaRenameOrigin;
import net.cserny.generated.RenamedMediaOptions;
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

import static net.cserny.support.UtilityProvider.toLoggableString;

@Order(2)
@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalRenameSearcher implements RenameSearcher {

    private final OnlineCacheRepository repository;
    private final OnlineProperties onlineConfig;
    private final TmdbRestApi tmdbRestApi;

    private final Pattern specialCharsRegex = Pattern.compile("[^a-zA-Z0-9-\s]");

    @Override
    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        List<MediaDescriptionData> mediaFound = switch (type) {
            case MOVIE -> searchMovie(nameYear);
            case TV -> searchTvShow(nameYear);
        };

        // TODO dont save duplicates?
        Set<OnlineCacheItem> items = convertAll(nameYear, mediaFound, type);
        log.info("Saving media found to cache {}", toLoggableString(items));
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
        TvResults tvResults = tmdbRestApi.searchTvShows(nameYear.name(), nameYear.year());
        if (tvResults == null || tvResults.getResults() == null) {
            log.info("No TV Shows found");
            return Collections.emptyList();
        }

        List<Tv> results = tvResults.getResults();
        if (results.isEmpty()) {
            log.info("No TV show results found");
            return Collections.emptyList();
        }

        List<Tv> sublist = results.subList(0, Math.min(results.size(), onlineConfig.getResultLimit()));
        log.info("TV show results found {}", toLoggableString(sublist));

        List<MediaDescriptionData> descriptions = new ArrayList<>();
        for (Tv tvSeries : sublist) {
            String posterUrl = producePosterUrl(tvSeries.getPosterPath());
            String title = processTitle(tvSeries.getName());
            String date = tvSeries.getFirstAirDate();
            String description = nullIfBlank(tvSeries.getOverview());
            List<String> cast = produceCast(tmdbRestApi.tvShowCredits(tvSeries.getId()));

            descriptions.add(new MediaDescriptionData().posterUrl(posterUrl).title(title)
                    .date(date).description(description).cast(cast));
        }

        return descriptions;
    }

    private List<MediaDescriptionData> searchMovie(NameYear nameYear) {
        MovieResults movieResults = tmdbRestApi.searchMovies(nameYear.name(), nameYear.year());
        if (movieResults == null || movieResults.getResults() == null) {
            log.info("No movies found");
            return Collections.emptyList();
        }

        List<Movie> results = movieResults.getResults();
        if (results.isEmpty()) {
            log.info("No movie results found");
            return Collections.emptyList();
        }

        List<Movie> sublist = results.subList(0, Math.min(results.size(), onlineConfig.getResultLimit()));
        log.info("Movie results found {}", toLoggableString(sublist));

        List<MediaDescriptionData> descriptions = new ArrayList<>();
        for (Movie movieDb : sublist) {
            String posterUrl = producePosterUrl(movieDb.getPosterPath());
            String title = processTitle(movieDb.getTitle());
            String date = movieDb.getReleaseDate();
            String description = nullIfBlank(movieDb.getOverview());
            List<String> cast = produceCast(tmdbRestApi.movieCredits(movieDb.getId()));

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

    private List<String> produceCast(TmdbRestApi.Credits credits) {
        if (credits == null) {
            return Collections.emptyList();
        }

        return credits.getCast().stream()
                .map(TmdbRestApi.Person::getName)
                .limit(onlineConfig.getResultLimit())
                .toList();
    }
}
