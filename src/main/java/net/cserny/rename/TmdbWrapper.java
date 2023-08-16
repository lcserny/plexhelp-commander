package net.cserny.rename;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Component
public class TmdbWrapper {

    @Autowired
    TmdbConfig tmdbConfig;

    @Autowired
    RestTemplate restTemplate;

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
        return restTemplate.getForObject(uri, Credits.class);    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MovieResults {

        @JsonProperty("page")
        Integer page;

        @JsonProperty("total_results")
        Long total_results;

        @JsonProperty("total_pages")
        Long total_pages;

        @JsonProperty("results")
        List<Movie> results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Movie {

        @JsonProperty("title")
        private String title;

        @JsonProperty("poster_path")
        private String posterPath;

        @JsonProperty("release_date")
        private String releaseDate;

        @JsonProperty("overview")
        private String overview;

        @JsonProperty("id")
        private Integer id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TvResults {

        @JsonProperty("page")
        Integer page;

        @JsonProperty("total_results")
        Long total_results;

        @JsonProperty("total_pages")
        Long total_pages;

        @JsonProperty("results")
        List<Tv> results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tv {

        @JsonProperty("name")
        private String name;

        @JsonProperty("poster_path")
        private String posterPath;

        @JsonProperty("first_air_date")
        private String firstAirDate;

        @JsonProperty("overview")
        private String overview;

        @JsonProperty("id")
        private Integer id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Credits {

        @JsonProperty("cast")
        private List<Person> cast;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Person {

        @JsonProperty("character")
        private String character;
    }
}
