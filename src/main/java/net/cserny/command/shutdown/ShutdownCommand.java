package net.cserny.command.shutdown;

import lombok.extern.slf4j.Slf4j;
import net.cserny.command.AbstractOSCommand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ShutdownCommand extends AbstractOSCommand {

    public static final String NAME = "shutdown";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected Process executeInternalLinux(Runtime runtime, String[] params) throws IOException {
        if (params.length == 0) {
            log.info("Shutting down system without delay");
            return runtime.exec(new String[]{"shutdown", "now"});
        } else if (params.length == 1 && StringUtils.isNumeric(params[0])) {
            log.info("Shutting down system in {} minutes", params[0]);
            return runtime.exec(new String[]{"shutdown", "+" + Integer.parseInt(params[0])});
        } else {
            log.info("Shutting down system with params: {}", Arrays.toString(params));
            List<String> execParams = new ArrayList<>();
            execParams.add("shutdown");
            execParams.addAll(List.of(params));

            return runtime.exec(execParams.toArray(new String[0]));
        }
    }

    @Override
    protected Process executeInternalWindows(Runtime runtime, String[] params) throws IOException {
        String commandBase = getCommandPrefix() + "shutdown.exe";

        if (params.length == 0) {
            log.info("Shutting down system without delay");
            return runtime.exec(new String[]{commandBase, "-s", "-f"});
        } else if (params.length == 1 && StringUtils.isNumeric(params[0])) {
            log.info("Shutting down system in {} minutes", params[0]);
            return runtime.exec(new String[]{commandBase, "-s", "-f", "-t", String.valueOf(60 * Integer.parseInt(params[0]))});
        } else {
            log.info("Shutting down system with params: {}", Arrays.toString(params));
            List<String> execParams = new ArrayList<>();
            execParams.add(commandBase);
            execParams.addAll(List.of(params));

            return runtime.exec(execParams.toArray(new String[0]));
        }
    }
}
