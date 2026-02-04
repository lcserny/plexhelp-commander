package net.cserny.support.events;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommanderEventPublisher {

    private final ApplicationEventPublisher publisher;

    public <R, E extends SyncEvent<R>> R process(E event) {
        publisher.publishEvent(event);
        R result = event.getResult();
        if (result == null) {
            throw new RuntimeException("Event was not processed correctly, no result was returned by an EventListener");
        }
        return result;
    }

    @Async
    public <E extends AsyncEvent> void send(E event) {
        publisher.publishEvent(event);
    }
}
