package net.cserny.command.restart;

import lombok.extern.slf4j.Slf4j;
import net.cserny.command.AbstractOSCommand;
import net.cserny.command.ServerCommandProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class RestartServiceCommand extends AbstractOSCommand {

    private static final String NAME = "restart-service";

    public RestartServiceCommand(ServerCommandProperties properties, TaskExecutor taskExecutor, TaskScheduler taskScheduler) {
        super(properties, taskExecutor, taskScheduler);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected List<String> produceCommandLinux(String[] params) {
        if (params.length == 2 && !StringUtils.isNumeric(params[1])) {
            String serviceName = params[1];
            return List.of("systemctl", "--user", "restart", serviceName);
        } else {
            throw new UnsupportedOperationException("Restarting Service with random params not supported " + Arrays.toString(params));
        }
    }

    @Override
    protected List<String> produceCommandWindows(String[] params) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
