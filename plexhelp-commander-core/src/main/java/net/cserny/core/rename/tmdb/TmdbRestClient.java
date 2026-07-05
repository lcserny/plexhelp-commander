package net.cserny.core.rename.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.cserny.config.TmdbProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class TmdbRestClient {

    private final TmdbProperties tmdbConfig;
    // FIXME use restClient
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        this.restTemplate = new RestTemplateBuilder()
                .connectTimeout(Duration.ofMillis(this.tmdbConfig.getConnectionTimeout()))
                .readTimeout(Duration.ofMillis(this.tmdbConfig.getReadTimeout()))
                .build();
    }

    public List<Tv> searchTvShows(String query, Integer year) {
        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString(tmdbConfig.getSearchTvUrl()).build()
                        .expand(tmdbConfig.getBaseUrl(), tmdbConfig.getApiKey(), query, year)
                        .encode();

        URI uri = uriComponents.toUri();
        TvResults results = restTemplate.getForObject(uri, TvResults.class);

        if (results == null) {
            return Collections.emptyList();
        }

        return results.getResults();    }

    public List<Movie> searchMovies(String query, Integer year) {
        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString(tmdbConfig.getSearchMoviesUrl()).build()
                        .expand(tmdbConfig.getBaseUrl(), tmdbConfig.getApiKey(), query, year)
                        .encode();

        URI uri = uriComponents.toUri();
        MovieResults results = restTemplate.getForObject(uri, MovieResults.class);

        if (results == null) {
            return Collections.emptyList();
        }

        return results.getResults();
    }

    public Credits movieCredits(int movieId) {
        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString(tmdbConfig.getMovieCreditsUrl()).build()
                        .expand(tmdbConfig.getBaseUrl(), movieId, tmdbConfig.getApiKey())
                        .encode();

        URI uri = uriComponents.toUri();
        return restTemplate.getForObject(uri, Credits.class);
    }

    public Credits tvShowCredits(int tvId) {
        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString(tmdbConfig.getTvCreditsUrl()).build()
                        .expand(tmdbConfig.getBaseUrl(), tvId, tmdbConfig.getApiKey())
                        .encode();

        URI uri = uriComponents.toUri();
        return restTemplate.getForObject(uri, Credits.class);
    }
}
