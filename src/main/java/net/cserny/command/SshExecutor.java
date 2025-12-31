package net.cserny.command;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.command.ServerCommandProperties.SshProperties;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SshExecutor implements OsExecutor {

    private static final long timeoutMs = 2000;
    private static final long intervalMs = 50;

    private final ExecutorService executorService;
    private final ServerCommandProperties properties;

    public ExecutionResponse execute(String command) throws Exception {
        log.info("Executing SSH command: {}", command);

        SshProperties sshProperties = properties.getSsh();

        JSch jsch = new JSch();
        Session session = jsch.getSession(sshProperties.getUsername(), sshProperties.getHost(), sshProperties.getPort());
        session.setPassword(sshProperties.getPassword());

        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.connect();

        Future<String> futureResponse = captureOutput(channel);
        int exitCode = waitFor(channel);

        channel.disconnect();
        session.disconnect();

        return new ExecutionResponse(exitCode, futureResponse.get());
    }

    private int waitFor(ChannelExec channel) throws InterruptedException {
        long elapsed = 0;
        while (!channel.isClosed() && elapsed < timeoutMs) {
            Thread.sleep(intervalMs);
            elapsed += intervalMs;
        }
        return channel.getExitStatus();
    }

    private Future<String> captureOutput(ChannelExec channel) {
        return executorService.submit(() -> {
            try (var inReader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
                 var errReader = new BufferedReader(new InputStreamReader(channel.getErrStream()));) {

                StringBuilder output = new StringBuilder();
                String line;

                while ((line = inReader.readLine()) != null) {
                    output.append(System.lineSeparator()).append(line);
                }
                while ((line = errReader.readLine()) != null) {
                    output.append(System.lineSeparator()).append(line);
                }

                return output.toString();
            } catch (IOException e) {
                return e.getMessage();
            }
        });
    }
}
