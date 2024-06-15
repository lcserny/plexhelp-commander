package net.cserny.command.shutdown;

import net.cserny.command.Command;
import net.cserny.command.CommandResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ShutdownCommand implements Command {

    public static final String NAME = "shutdown";

    @Override
    public CommandResponse execute(String[] params) {
        List<String> paramsList = new ArrayList<>();
        paramsList.add(NAME);

        String os = System.getProperty("os.name");
        if (os.contains("win")) {
            buildParamsWindows(paramsList, params);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            buildParamsUnix(paramsList, params);
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(paramsList.toArray(new String[0]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return CommandResponse.EMPTY;
    }

    private void buildParamsUnix(List<String> paramsList, String[] params) {
        if (params == null || params.length == 0) {
            paramsList.add("now");
        } else {
            Collections.addAll(paramsList, params);
        }
    }

    private void buildParamsWindows(List<String> paramsList, String[] params) {
        if (params == null || params.length == 0) {
            paramsList.add("-s");
        } else {
            Collections.addAll(paramsList, params);
        }
    }

    @Override
    public String name() {
        return NAME;
    }
}
