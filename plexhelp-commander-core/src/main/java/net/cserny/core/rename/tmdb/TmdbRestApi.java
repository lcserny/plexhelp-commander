package net.cserny.core.rename.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.cserny.api.QBitTorrentRestApi;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange(TmdbRestApi.Routes.BASE_URI)
public interface TmdbRestApi {

    // TODO
    @PostExchange(TmdbRestApi.Routes.SEARCH_MOVIES_URI)
    ResponseEntity<Void> searchMovies(@RequestHeader HttpHeaders headers,
                                      @RequestBody MultiValueMap<String, String> formParams);


    interface Routes {

        String BASE_URI = "/3";
        String SEARCH_MOVIES_URI = "/search/movie?api_key={tmdbApiKey}&query={query}&year={year}";
        String SEARCH_TV_URI = "/search/tv?api_key={tmdbApiKey}&query={query}&year={year}";
        String MOVIE_CREDITS_URI = "/movie/{movieId}/credits?api_key={tmdbApiKey}";
        String TV_CREDITS_URI = "/tv/{tvId}/credits?api_key={tmdbApiKey}";
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    class MovieResults {

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
    class Movie {

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
    class TvResults {

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
    class Tv {

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
    class Credits {

        @JsonProperty("cast")
        private List<Person> cast;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Person {

        @JsonProperty("name")
        private String name;
    }
}
