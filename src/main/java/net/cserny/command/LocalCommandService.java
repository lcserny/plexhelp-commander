package net.cserny.command;

import io.quarkus.arc.All;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

// TODO: add tests
@Singleton
public class LocalCommandService {

    private static final Logger LOGGER = Logger.getLogger(LocalCommandService.class);

    @Inject
    @All
    List<Command> commands;

    public CommandResponse execute(String name, Param[] params) {
        for (Command command : commands) {
            if (command.name().equals(name)) {
                try {
                    return command.execute(params);
                } catch (Exception e) {
                    LOGGER.warnv("Error occurred executing command {0}: {1}", name, e.getMessage());
                    return new CommandResponse(CommandResponse.Status.FAILED);
                }
            }
        }
        return new CommandResponse(CommandResponse.Status.NOT_FOUND);
    }
}
