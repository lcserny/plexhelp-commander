package net.cserny.command.shutdown;

import lombok.extern.slf4j.Slf4j;
import net.cserny.command.AbstractShutdownCommand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ShutdownCommand extends AbstractShutdownCommand {

    public static final String NAME = "shutdown";

    protected void appendLinuxParams(List<String> outParams, String[] inParams) {
        if (inParams.length == 0) {
            outParams.add("now");
        } else if (inParams.length == 1 && StringUtils.isNumeric(inParams[0])) {
            outParams.add("+" + Integer.parseInt(inParams[0]));
        } else {
            Collections.addAll(outParams, inParams);
        }
    }

    protected void appendWindowsParams(List<String> outParams, String[] inParams) {
        if (inParams.length == 0) {
            outParams.add("-s");
        } else if (inParams.length == 1 && StringUtils.isNumeric(inParams[0])) {
            outParams.add("-s");
            outParams.add("-t");
            outParams.add(String.valueOf(60 * Integer.parseInt(inParams[0])));
        } else {
            Collections.addAll(outParams, inParams);
        }
    }

    @Override
    public String name() {
        return NAME;
    }
}
