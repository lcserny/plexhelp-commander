package net.cserny;

import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Profile("test")
@Configuration
@ComponentScan({
        "net.cserny.search",
        "net.cserny.rename",
        "net.cserny.move",
        "net.cserny.filesystem",
        "net.cserny.download",
})
public class TestConfig {

    @Bean
    public Tracer tracer() {
        return Tracer.NOOP;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
