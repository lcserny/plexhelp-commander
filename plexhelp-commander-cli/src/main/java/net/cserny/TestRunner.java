package net.cserny;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;

// TODO: impl spring shell with an enum for the operation to do and such

@Slf4j
@SpringBootApplication
public class TestRunner implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TestRunner.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Running TestRunner");
    }
}
