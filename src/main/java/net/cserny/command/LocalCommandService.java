package net.cserny.command;

import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.CommandResponse;
import net.cserny.generated.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.String.format;

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
                    log.warn(format("Error occurred executing command: %s", name), e);
                    return new CommandResponse().status(Status.FAILED);
                }
            }
        }
        return new CommandResponse().status(Status.NOT_FOUND);
    }
}
