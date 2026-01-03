package net.cserny;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

@Import(IntegrationTestWithRealFS.RealFSTestConfig.class)
public abstract class IntegrationTestWithRealFS extends BaseIntegrationTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class RealFSTestConfig {

        @Primary
        @Bean
        FileSystem testFileSystem() {
            return FileSystems.getDefault();
        }
    }
}
