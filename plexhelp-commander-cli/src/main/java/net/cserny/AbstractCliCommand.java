package net.cserny;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public abstract class AbstractCliCommand implements Callable<Integer> {

    protected abstract void run() throws Exception;

    @Override
    public Integer call() {
        try {
            run();
            return 0;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return 1;
        }
    }
}
