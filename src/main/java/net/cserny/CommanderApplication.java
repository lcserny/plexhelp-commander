package net.cserny;

import io.v47.tmdb.TmdbClient;
import io.v47.tmdb.autoconfigure.TmdbAutoConfiguration;
import io.v47.tmdb.http.ContextWebClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class CommanderApplication {

    @Value("${tmdb.client.api-key}")
    private String apiKey;

    @Autowired
    ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(CommanderApplication.class, args);
    }

    @Bean
    public TmdbClient tmdbClient() {
        return new TmdbClient(new ContextWebClientFactory(context), apiKey);
    }
}
