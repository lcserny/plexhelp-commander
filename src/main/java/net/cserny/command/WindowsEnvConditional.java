package net.cserny.command;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class WindowsEnvConditional implements Condition {
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String wsl = context.getEnvironment().getProperty("server.command.wsl");
        boolean isWsl = Boolean.parseBoolean(wsl);
        boolean isWindows = Objects.requireNonNull(context.getEnvironment().getProperty("os.name")).contains("Win");

        return isWindows || isWsl;
    }
}
