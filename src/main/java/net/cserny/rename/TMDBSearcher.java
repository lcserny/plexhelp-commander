package net.cserny.rename;

import io.smallrye.mutiny.Multi;
import io.v47.tmdb.model.*;
import lombok.extern.slf4j.Slf4j;
import net.cserny.rename.NameNormalizer.NameYear;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.regex.Pattern;

@Order(2)
@Component
@Slf4j
public class TMDBSearcher implements Searcher {

    @Autowired
    OnlineCacheRepository repository;

    @Autowired
    OnlineConfig onlineConfig;

    @Autowired
    TmdbWrapper tmdbWrapper;

    private final Pattern specialCharsRegex = Pattern.compile("[^a-zA-Z0-9-\s]");

    @Override
    public RenamedMediaOptions search(NameYear nameYear, MediaFileType type) {
        List<MediaDescription> mediaFound = switch (type) {
            case MOVIE -> searchMovie(nameYear);
            case TV -> searchTvShow(nameYear);
        };

        repository.saveAllOnlineCacheItem(nameYear, mediaFound, type);

        return new RenamedMediaOptions(MediaRenameOrigin.TMDB, mediaFound);
    }

    private List<MediaDescription> searchTvShow(NameYear nameYear) {
        PaginatedListResults<TvListResult> page = pullFromPublisher(tmdbWrapper.searchTvShows(nameYear.name(), nameYear.year()));
        if (page == null) {
            log.warn("TMDB Search could not search TV, check configuration");
            return Collections.emptyList();
        }

        List<TvListResult> results = page.getResults();
        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        List<TvListResult> sublist = results.subList(0, Math.min(results.size(), onlineConfig.getResultLimit()));

        List<MediaDescription> descriptions = new ArrayList<>();
        for (TvListResult tvSeries : sublist) {
            String posterUrl = producePosterUrl(tvSeries.getPosterPath());
            String title = processTitle(tvSeries.getName());
            LocalDate date = tvSeries.getFirstAirDate();
            String description = nullIfBlank(tvSeries.getOverview());
            List<String> cast = produceCast(pullFromPublisher(tmdbWrapper.tvShowCredits(tvSeries.getId())));

            descriptions.add(new MediaDescription( posterUrl, title, date, description, cast ));
        }

        return descriptions;
    }

    private List<MediaDescription> searchMovie(NameYear nameYear) {
        PaginatedListResults<MovieListResult> page = pullFromPublisher(tmdbWrapper.searchMovies(nameYear.name(), nameYear.year()));
        if (page == null) {
            log.warn("TMDB Search could not search Movies, check configuration");
            return Collections.emptyList();
        }

        List<MovieListResult> results = page.getResults();
        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        List<MovieListResult> sublist = results.subList(0, Math.min(results.size(), onlineConfig.getResultLimit()));

        List<MediaDescription> descriptions = new ArrayList<>();
        for (MovieListResult movieDb : sublist) {
            String posterUrl = producePosterUrl(movieDb.getPosterPath());
            String title = processTitle(movieDb.getTitle());
            LocalDate date = movieDb.getReleaseDate();
            String description = nullIfBlank(movieDb.getOverview());
            List<String> cast = produceCast(pullFromPublisher(tmdbWrapper.movieCredits(movieDb.getId())));

            descriptions.add(new MediaDescription( posterUrl, title, date, description, cast ));
        }

        return descriptions;
    }

    private String processTitle(String title) {
        return title.replaceAll("&", "and")
                .replaceAll(specialCharsRegex.pattern(), "");
    }

    private LocalDate produceDate(String releaseDate) {
        return StringUtils.isBlank(releaseDate) ? null : LocalDate.parse(releaseDate, DateTimeFormatter.ISO_DATE);
    }

    private String producePosterUrl(String posterPath) {
        return StringUtils.isBlank(posterPath) ? null : onlineConfig.getPosterBase() + posterPath;
    }

    private String nullIfBlank(String text) {
        return StringUtils.isBlank(text) ? null : text;
    }

    private <T> T pullFromPublisher(Flow.Publisher<T> publisher) {
        if (publisher == null) {
            return null;
        }

        return Multi.createFrom()
                .publisher(publisher)
                .subscribe()
                .asIterable()
                .stream().findFirst().get();
    }

    private List<String> produceCast(TvShowCredits credits) {
        if (credits == null) {
            return Collections.emptyList();
        }

        return credits.getCast().stream()
                .map(CreditListResult::getName)
                .limit(onlineConfig.getResultLimit())
                .toList();
    }

    private List<String> produceCast(MovieCredits credits) {
        if (credits == null) {
            return Collections.emptyList();
        }

        return credits.getCast().stream()
                .map(CreditListResult::getName)
                .limit(onlineConfig.getResultLimit())
                .toList();
    }
}
