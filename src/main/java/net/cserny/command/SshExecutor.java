package net.cserny.command;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.command.ServerCommandProperties.SshProperties;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SshExecutor implements OsExecutor {

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

        InputStream input = channel.getInputStream();
        channel.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
            output.append(System.lineSeparator());
        }

        int exitCode = channel.getExitStatus();
        String response = output.toString();

        channel.disconnect();
        session.disconnect();

        return new ExecutionResponse(exitCode, response);
    }
}
