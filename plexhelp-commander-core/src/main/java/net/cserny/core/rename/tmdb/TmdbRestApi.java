package net.cserny.core.rename.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange(value = TmdbRestApi.Routes.BASE_URI, accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
public interface TmdbRestApi {

    @GetExchange(Routes.SEARCH_MOVIES_URI)
    MovieResults searchMovies(@RequestParam String query, @RequestParam(required = false) Integer year);

    @GetExchange(Routes.SEARCH_TV_URI)
    TvResults searchTvShows(@RequestParam String query, @RequestParam(required = false) Integer year);

    @GetExchange(Routes.MOVIE_CREDITS_URI)
    Credits movieCredits(@PathVariable Integer movieId);

    @GetExchange(Routes.TV_CREDITS_URI)
    Credits tvShowCredits(@PathVariable Integer tvId);

    interface Routes {

        String BASE_URI = "/3";
        String API_KEY_PARAM = "tmdbApiKey";

        String SEARCH_MOVIES_URI = "/search/movie?api_key={" + API_KEY_PARAM + "}";
        String SEARCH_TV_URI = "/search/tv?api_key={" + API_KEY_PARAM + "}";
        String MOVIE_CREDITS_URI = "/movie/{movieId}/credits?api_key={" + API_KEY_PARAM + "}";
        String TV_CREDITS_URI = "/tv/{tvId}/credits?api_key={" + API_KEY_PARAM + "}";
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
