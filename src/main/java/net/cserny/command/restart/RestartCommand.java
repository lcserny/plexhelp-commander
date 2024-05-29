package net.cserny.command.restart;

import net.cserny.command.Command;
import net.cserny.command.CommandResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RestartCommand implements Command {

    public static final String NAME = "reboot";

    @Override
    public CommandResponse execute(String[] params) {
        List<String> paramsList = List.of("reboot");

        String os = System.getProperty("os.name");
        if (os.contains("win")) {
            throw new RuntimeException("Reboot command not available for Windows OS");
        } 

        try {
            ProcessBuilder builder = new ProcessBuilder(paramsList);
            builder.start();
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
