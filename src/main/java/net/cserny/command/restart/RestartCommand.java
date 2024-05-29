package net.cserny.command.restart;

import net.cserny.command.Command;
import net.cserny.command.CommandResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RestartCommand implements Command {

    public static final String NAME = "reboot";

    @Override
    public CommandResponse execute(String[] params) {
        List<String> paramsList = new ArrayList<>();
        paramsList.add(NAME);

        String os = System.getProperty("os.name");
        if (os.contains("win")) {
            throw new RuntimeException("Reboot command not available for Windows OS");
        } 

        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(paramsList.toArray(new String[0]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return CommandResponse.EMPTY;
    }

    @Override
    public String name() {
        return NAME;
    }
}
