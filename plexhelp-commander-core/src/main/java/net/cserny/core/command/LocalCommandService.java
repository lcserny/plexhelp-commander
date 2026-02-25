package net.cserny.core.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.core.command.CommandRunner.CommandResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static net.cserny.support.UtilityProvider.toLoggableString;

@RequiredArgsConstructor
@Service
@Slf4j
public class LocalCommandService {

    private final List<Command> commands;

    public Optional<CommandResult> execute(String name, String[] params) {
        for (Command command : commands) {
            if (command.name().equals(name)) {
                try {
                    log.info("Executing command: {} with params {}", command.name(), toLoggableString(params));
                    return command.execute(params);
                } catch (Exception e) {
                    log.warn("Error occurred executing command {}: {}", name, e.getMessage());
                    return Optional.of(new CommandResult(1, e.getMessage()));
                }
            }
        }
        return Optional.empty();
    }
}
