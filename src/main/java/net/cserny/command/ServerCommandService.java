package net.cserny.command;

import io.quarkus.scheduler.Scheduled;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class ServerCommandService {

    @Inject
    ServerCommandRepository repository;

    @Inject
    ServerCommandConfig config;

    // TODO: inject list of Commands (interface), Shutdown is one but also have one for testing like PrintCommand

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
