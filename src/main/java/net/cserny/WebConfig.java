package net.cserny;

import net.cserny.command.Param;
import net.cserny.rename.TmdbWrapper;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@RegisterReflectionForBinding({
        TmdbWrapper.MovieResults.class,
        TmdbWrapper.TvResults.class,
        TmdbWrapper.Movie.class,
        TmdbWrapper.Tv.class,
        TmdbWrapper.Credits.class,
        TmdbWrapper.Person.class,
        Param.class
})
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowCredentials(false)
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("Content-Type", "Access-Control-Allow-Origin");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
