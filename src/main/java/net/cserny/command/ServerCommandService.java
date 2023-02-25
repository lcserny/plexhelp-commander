package net.cserny.command;

import io.quarkus.arc.All;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.scheduler.Scheduled;
import net.cserny.command.shutdown.CommandResponse;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class ServerCommandService {

    private static final Logger LOGGER = Logger.getLogger(ServerCommandService.class);

    @Inject
    ServerCommandRepository repository;

    @Inject
    ServerCommandConfig config;

    @Inject
    @All
    List<Command> commands;

    @PostConstruct
    public void initServerCommand() {
        LOGGER.info("Initializing " + config.name());
        Optional<ServerCommand> serverCommand = repository.getByServerName(config.name());
        if (serverCommand.isPresent()) {
            LOGGER.info("Found remote server command with name " + config.name());
            ServerCommand server = serverCommand.get();
            server.actionsPending = new ArrayList<>();
            repository.updateServerCommand(server);
        } else {
            LOGGER.info("Remote server command with name " + config.name() + " not found, creating...");
            ServerCommand server = new ServerCommand();
            server.serverName = config.name();
            server.actionsAvailable = commands.stream().map(Command::name).toList();
            repository.saveServer(server);
        }
    }

    @Scheduled(cron = "{server.command.listen.cron}")
    public void startListeningForActions() {
        repository.setLastPingDate(config.name(), System.currentTimeMillis());

        repository.getByServerName(config.name()).ifPresent(server -> {
            LOGGER.info("Checking for pending actions for server " + config.name());
            if (server.actionsPending != null && !server.actionsPending.isEmpty()) {
                String firstActionPending = server.actionsPending.remove(0);
                repository.updateServerCommand(server);

                for (Command command : commands) {
                    if (command.name().equals(firstActionPending)) {
                        LOGGER.info("Executing action " + command.name() + " for server " + config.name());
                        command.execute();
                    }
                }
            }
        });
    }
}
