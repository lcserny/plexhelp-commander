package net.cserny.command;

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.command.ServerCommandProperties.SshProperties;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

@Slf4j
@RequiredArgsConstructor
public class SshExecutor implements OsExecutor {

    private static final int TIMEOUT_SECONDS = 2;

    private final ServerCommandProperties properties;

    @Override
    public ExecutionResponse execute(String command) throws Exception {
        log.info("Executing SSH command via SSHJ: {}", command);

        SshProperties sshProperties = properties.getSsh();

        SSHClient ssh = new SSHClient();
        try (ssh) {
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(sshProperties.getHost(), sshProperties.getPort());
            ssh.authPassword(sshProperties.getUsername(), sshProperties.getPassword());

            try (Session session = ssh.startSession()) {
                final Session.Command cmd = session.exec(command);

                String output = IOUtils.readFully(cmd.getInputStream()).toString() +
                        IOUtils.readFully(cmd.getErrorStream()).toString();

                cmd.join(TIMEOUT_SECONDS, TimeUnit.SECONDS);

                Integer exitStatus = cmd.getExitStatus();
                return new ExecutionResponse(exitStatus != null ? exitStatus : -1, output.trim());
            }
        } finally {
            if (ssh.isConnected()) {
                ssh.disconnect();
            }
        }
    }
}
