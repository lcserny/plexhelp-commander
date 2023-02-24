package net.cserny.command;

import io.quarkus.arc.All;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.scheduler.Scheduled;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Dependent
public class ServerCommandService {

    @Inject
    ServerCommandRepository repository;

    @Inject
    ServerCommandConfig config;

    @Inject
    @All
    List<Command> commands;

    @PostConstruct
    public void initServerCommand() {
        ServerCommand server = repository.getByServerName(config.name());
        server.actionsPending = new ArrayList<>();
        server.actionsAvailable = commands.stream().map(Command::name).toList();
        repository.updateServerCommand(server);
    }

    @Scheduled(every = "{server.command.listen.interval}")
    public void startListeningForActions() {
        repository.setLastPingDate(config.name(), System.currentTimeMillis());

        ServerCommand server = repository.getByServerName(config.name());
        if (!server.actionsPending.isEmpty()) {
            String firstActionPending = server.actionsPending.remove(0);
            repository.updateServerCommand(server);
            for (Command command : commands) {
                if (command.name().equals(firstActionPending)) {
                    command.execute();
                }
            }
        }
    }
}
