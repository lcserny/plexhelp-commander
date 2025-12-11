package net.cserny.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DummyProcess extends Process {

    public static final Process INSTANCE = new DummyProcess(0, "DONE");

    private final int exitCode;
    private final String stdOutput;

    public DummyProcess(int exitCode, String stdOutput) {
        this.exitCode = exitCode;
        this.stdOutput = stdOutput;
    }

    @Override
    public OutputStream getOutputStream() {
        return new ByteArrayOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(stdOutput.getBytes());
    }

    @Override
    public InputStream getErrorStream() {
        return new ByteArrayInputStream("".getBytes());
    }

    @Override
    public int waitFor() throws InterruptedException {
        return exitCode;
    }

    @Override
    public int exitValue() {
        return exitCode;
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean isAlive() {
        return false;
    }
}
