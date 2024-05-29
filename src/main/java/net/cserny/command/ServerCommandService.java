package net.cserny.command;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Deprecated
@Service
@Slf4j
public class ServerCommandService {

    @Autowired
    ServerCommandRepository repository;

    @Autowired
    ServerCommandProperties config;

    @Autowired
    List<Command> commands;

    @PostConstruct
    public void initServerCommand() {
        log.info("Initializing " + config.getName());
        Optional<ServerCommand> serverCommand = repository.getByServerName(config.getName());
        if (serverCommand.isPresent()) {
            log.info("Found remote server command with name " + config.getName());
            ServerCommand server = serverCommand.get();
            server.actionsPending = new ArrayList<>();
            repository.save(server);
        } else {
            log.info("Remote server command with name " + config.getName() + " not found, creating...");
            ServerCommand server = new ServerCommand();
            server.serverName = config.getName();
            server.actionsAvailable = commands.stream().map(Command::name).toList();
            repository.save(server);
        }
    }

    // EnableScheduledJobs needed on Configuration somewhere
//    @Scheduled(cron = "${server.command.listen.cron}")
    public void startListeningForActions() {
        repository.getByServerName(config.getName()).ifPresent(server -> {
            server.lastPingDate = System.currentTimeMillis();
            repository.save(server);
        });


        repository.getByServerName(config.getName()).ifPresent(server -> {
            if (server.actionsPending != null && !server.actionsPending.isEmpty()) {
                String firstActionPending = server.actionsPending.remove(0);
                repository.save(server);

                for (Command command : commands) {
                    if (command.name().equals(firstActionPending)) {
                        log.info("Executing action " + command.name() + " for server " + config.getName());
                        command.execute(new String[]{});
                    }
                }
            }
        });
    }
}
