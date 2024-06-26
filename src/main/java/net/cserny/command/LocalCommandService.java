package net.cserny.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LocalCommandService {

    @Autowired
    List<Command> commands;

    public CommandResponse execute(String name, String[] params) {
        for (Command command : commands) {
            if (command.name().equals(name)) {
                try {
                    log.info("Executing command: {} with params {}", command.name(), params);
                    return command.execute(params);
                } catch (Exception e) {
                    log.warn("Error occurred executing command {}: {}", name, e.getMessage());
                    return new CommandResponse(CommandResponse.Status.FAILED);
                }
            }
        }
        return new CommandResponse(CommandResponse.Status.NOT_FOUND);
    }
}
