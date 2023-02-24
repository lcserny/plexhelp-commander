package net.cserny.command;

import io.quarkus.arc.All;
import io.quarkus.scheduler.Scheduled;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

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
    public void cleanPendingActions() {
        // TODO: when app starts empty pendingActions and add actionsAvailable (server can be controlled only when its online)
    }

    @Scheduled(every = "{server.command.listen.interval}")
    public void startListeningForActions() {
        // TODO:
        // - get serverCommand by serverName configured, save in var
        // - update serverCommand lastPingDate to current timestamp unixnano
        // - on serverCommand var
        //   - get first actionPending, save to var
        //   - update serverCommand actionsPending to remaining actionsPending (without the one saved to var)
        //   - execute the action from the var
    }
}
