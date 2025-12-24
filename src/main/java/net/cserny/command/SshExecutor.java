package net.cserny.command;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
@Service
@RequiredArgsConstructor
public class SshExecutor {

    // TODO externalize this
    //  - rename AbstractOSCommand in AbstractSSHOSCommand
    //  - adapt abstract command to use SshExecutor

    private static final String user = "someUser";
    private static final String pass = "somePass";
    private static final String host = "192.168.1.10";
    private static final int port = 22;

    public SshResponse execute(String command) throws IOException, JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(pass);

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

        return new SshResponse(exitCode, response);
    }

    public record SshResponse(int exitCode, String response) {}
}
