package net.cserny.core.command.restart;

import net.cserny.core.command.AbstractOSCommand;
import net.cserny.core.command.CommandRunner;
import net.cserny.config.ServerCommandProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static net.cserny.support.UtilityProvider.escaped;

@Component
public class RestartServiceCommand extends AbstractOSCommand<String> {

    public RestartServiceCommand(ServerCommandProperties properties,
                                 TaskScheduler taskScheduler,
                                 CommandRunner commandRunner) {
        super(properties, taskScheduler, commandRunner);
    }

    @Override
    public CommandName name() {
        return CommandName.RESTART_SERVICE;
    }

    @Override
    protected boolean executeNow() {
        return true;
    }

    @Override
    protected List<String> produceCommandLinux(String[] params) {
        String serviceName = getServiceName(params);
        return List.of("systemctl", "--user", "restart", serviceName);
    }

    @Override
    protected String adaptOutput(String output) {
        return output;
    }

    @Override
    protected List<String> produceCommandWindows(String[] params) {
        String serviceName = getServiceName(params);
        return List.of(getSystem32Prefix() + "WindowsPowerShell/v1.0/powershell.exe", "-Command", escaped("Restart-Service -Name '" + serviceName + "' -Force"));
    }

    private String getServiceName(String[] params) {
        if (params.length == 2 && !StringUtils.isNumeric(params[1])) {
            return params[1];
        }
        throw new UnsupportedOperationException("Restarting Service with random params not supported " + Arrays.toString(params));
    }
}
