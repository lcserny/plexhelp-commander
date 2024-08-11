package net.cserny;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

@Service
public class VirtualExecutor {

    @Autowired
    private Tracer tracer;

    private ExecutorService threadPool;

    @PostConstruct
    public void init() {
        this.threadPool = Executors.newVirtualThreadPerTaskExecutor();
    }

    public <T> List<T> executeWithCurrentSpan(Stream<Callable<T>> tasks) {
        Span currentSpan = this.tracer.currentSpan();
        List<Callable<T>> toProcess = new ArrayList<>();
        tasks.forEach(w -> toProcess.add(() -> {
            try (Tracer.SpanInScope ignored = this.tracer.withSpan(currentSpan)) {
                return w.call();
            }
        }));
        return this.executeInternal(toProcess);
    }

    public <T> List<T> executeWithNewSpans(Stream<Callable<T>> tasks) {
        List<Callable<T>> toProcess = new ArrayList<>();
        tasks.forEach(w -> {
            Span nextSpan = this.tracer.nextSpan();
            toProcess.add(() -> {
                try (Tracer.SpanInScope ignored = this.tracer.withSpan(nextSpan.start())) {
                    return w.call();
                } finally {
                    nextSpan.end();
                }
            });
        });
        return this.executeInternal(toProcess);
    }

    private  <T> List<T> executeInternal(List<Callable<T>> toProcess) {
        List<T> results = new ArrayList<>();
        List<Future<T>> futures;
        try {
            futures = this.threadPool.invokeAll(toProcess);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Future<T> f : futures) {
            try {
                results.add(f.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        return results;
    }
}
