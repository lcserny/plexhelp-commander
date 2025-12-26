package net.cserny.command.restart;

import lombok.extern.slf4j.Slf4j;
import net.cserny.command.AbstractOSCommand;
import net.cserny.command.OsExecutor;
import net.cserny.command.ServerCommandProperties;
import net.cserny.command.SshExecutor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class RestartServiceCommand extends AbstractOSCommand {

    private static final String NAME = "restart-service";

    public RestartServiceCommand(ServerCommandProperties properties,
                                 TaskScheduler taskScheduler,
                                 OsExecutor osExecutor) {
        super(properties, taskScheduler, osExecutor);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected List<String> produceCommandLinux(String[] params) {
        String serviceName = getServiceName(params)
                .orElseThrow(() -> new UnsupportedOperationException("Restarting Linux service with random params not supported " + Arrays.toString(params)));
        return List.of("systemctl", "--user", "restart", serviceName);
    }

    @Override
    protected List<String> produceCommandWindows(String[] params) {
        String serviceName = getServiceName(params)
                .orElseThrow(() -> new UnsupportedOperationException("Restarting Windows service with random params not supported " + Arrays.toString(params)));
        return List.of(getCommandPrefix() + "powershell.exe", "-Command", "\"Restart-Service -Name '" + serviceName + "' -Force\"");
    }

    private Optional<String> getServiceName(String[] params) {
        if (params.length == 2 && !StringUtils.isNumeric(params[1])) {
            return Optional.of(params[1]);
        }
        return Optional.empty();
    }
}
