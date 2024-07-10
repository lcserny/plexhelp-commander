package net.cserny.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

import java.util.Objects;

@Slf4j
public class LinuxEnvConditional implements Condition {
    public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        log.info("Checking if Linux");

        String wsl = context.getEnvironment().getProperty("server.command.wsl");
        if (Boolean.parseBoolean(wsl)) {
            log.info("wsl is true, returning false");
            return false;
        }

        log.info("Wsl not detected");

        return (Objects.requireNonNull(context.getEnvironment().getProperty("os.name")).contains("nux")
                || Objects.requireNonNull(context.getEnvironment().getProperty("os.name")).contains("aix"));
    }
}
