package net.cserny.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
@RequiredArgsConstructor
public class CommandCommandRunner implements CommandRunner {

    private final ExecutorService executorService;

    public CommandResponse run(String command) throws Exception {
        log.info("Executing command: {}", command);

        ProcessBuilder builder = new ProcessBuilder();
        builder.redirectErrorStream(true);
        builder.command(command);

        Process process = builder.start();
        Future<String> futureResponse = captureOutput(process);
        int exitCode = process.waitFor();

        return new CommandResponse(exitCode, futureResponse.get());
    }

    private Future<String> captureOutput(Process process) {
        return executorService.submit(() -> {
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(System.lineSeparator()).append(line);
                }
                return output.toString();
            } catch (IOException e) {
                return e.getMessage();
            }
        });
    }
}
