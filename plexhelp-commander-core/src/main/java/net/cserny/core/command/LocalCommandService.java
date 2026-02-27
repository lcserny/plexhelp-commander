package net.cserny.core.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.api.CommandExecutingService;
import net.cserny.api.dto.CommandResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static net.cserny.support.UtilityProvider.toLoggableString;

@RequiredArgsConstructor
@Service
@Slf4j
public class LocalCommandService implements CommandExecutingService {

    private final List<Command> commands;

    @Override
    public <T> Optional<CommandResult<T>> execute(String name, String[] params) {
        for (Command<T> command : commands) {
            if (command.name().equals(name)) {
                try {
                    log.info("Executing command: {} with params {}", command.name(), toLoggableString(params));
                    return command.execute(params);
                } catch (Exception e) {
                    log.warn("Error occurred executing command {}: {}", name, e.getMessage());
                    return Optional.of(new CommandResult<>(false, false, null));
                }
            }
        }
        return Optional.empty();
    }
}
