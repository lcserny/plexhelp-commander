package net.cserny;

import jakarta.validation.constraints.NotNull;
import net.cserny.rename.TmdbWrapper;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebMvc
@RegisterReflectionForBinding({
        TmdbWrapper.MovieResults.class,
        TmdbWrapper.TvResults.class,
        TmdbWrapper.Movie.class,
        TmdbWrapper.Tv.class,
        TmdbWrapper.Credits.class,
        TmdbWrapper.Person.class,
        DataMapperImpl.class
})
public class WebConfig implements WebMvcConfigurer {

    @Value("${http.client.connection.timeout.ms}")
    private int connectionTimeoutMs;

    @Value("${http.client.read.timeout.ms}")
    private int readTimeoutMs;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "OPTIONS", "DELETE")
                .allowedHeaders("Content-Type", "Authorization");
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(this.connectionTimeoutMs))
                .setReadTimeout(Duration.ofMillis(this.readTimeoutMs))
                .build();
    }

    public static Pageable createPageable(Integer page, Integer size, List<String> sort) {
        if (page == null || size == null || sort.isEmpty()) {
            return Pageable.unpaged();
        }
        return PageRequest.of(page, size, parseSort(sort));
    }

    public static Sort parseSort(@NotNull List<String> sorts) {
        if (sorts.size() == 1) {
            return Sort.by(sorts.getFirst());
        }

        if (sorts.size() == 2 && !sorts.getFirst().contains(",")) {
            String prop = sorts.get(0);
            String direction = sorts.get(1);
            return Sort.by(new Sort.Order(Sort.Direction.valueOf(direction), prop));
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (String sort : sorts) {
            String[] split = sort.split(",");
            String prop = split[0];
            if (split.length > 1) {
                String direction = split[1];
                orders.add(new Sort.Order(Sort.Direction.valueOf(direction), prop));
            } else {
                orders.add(Sort.Order.by(prop));
            }
        }
        return Sort.by(orders);
    }
}
