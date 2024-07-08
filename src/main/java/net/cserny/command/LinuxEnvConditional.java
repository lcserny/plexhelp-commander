package net.cserny.command;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class LinuxEnvConditional implements Condition {
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean isLinux = (Objects.requireNonNull(context.getEnvironment().getProperty("os.name")).contains("nux")
                || Objects.requireNonNull(context.getEnvironment().getProperty("os.name")).contains("aix"));

        if (isLinux) {
            String wsl = context.getEnvironment().getProperty("server.command.wsl");
            return !Boolean.parseBoolean(wsl);
        }

        return false;
    }
}
