package net.cserny.support.events;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommanderEventPublisher {

    private final ApplicationEventPublisher publisher;

    public <E extends SyncEvent<?>> void processSync(E event) {
        publisher.publishEvent(event);
    }

    @Async
    public <E extends AsyncEvent> void sendAsync(E event) {
        publisher.publishEvent(event);
    }
}
