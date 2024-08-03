package net.cserny;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class VirtualExecutor {

    private ExecutorService threadPool;

    @PostConstruct
    public void init() {
        this.threadPool = Executors.newVirtualThreadPerTaskExecutor();
    }

    // TODO: add span impl
    public <T> Future<T> execute(Callable<T> task) {
        return this.threadPool.submit(task);
    }

    public <T> List<Future<T>> execute(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return this.threadPool.invokeAll(tasks);
    }
}
