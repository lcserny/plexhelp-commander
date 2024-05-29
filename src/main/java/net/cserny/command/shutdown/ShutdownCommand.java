package net.cserny.command.shutdown;

import net.cserny.command.Command;
import net.cserny.command.CommandResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShutdownCommand implements Command {

    public static final String NAME = "shutdown";

    @Override
    public CommandResponse execute(String[] params) {
        List<String> paramsList = List.of("shutdown");

        String os = System.getProperty("os.name");
        if (os.contains("win")) {
            buildParamsWindows(paramsList, params);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            buildParamsUnix(paramsList, params);
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(paramsList);
            builder.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return CommandResponse.EMPTY;
    }

    private void buildParamsUnix(List<String> paramsList, String[] params) {
        if (params == null || params.length == 0) {
            paramsList.add("now");
        } else {
            for (String p : params) {
                paramsList.add(p);
            }
        }
    }

    private void buildParamsWindows(List<String> paramsList, String[] params) {
        if (params == null || params.length == 0) {
            paramsList.add("-s");
        } else {
            for (String p : params) {
                paramsList.add(p);
            }
        }
    }

    @Override
    public String name() {
        return NAME;
    }
}
