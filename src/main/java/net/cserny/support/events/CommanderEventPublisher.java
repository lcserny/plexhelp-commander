package net.cserny.support.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.support.events.Events.CommanderEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommanderEventPublisher {

    private final ApplicationEventPublisher publisher;

    public <E extends CommanderEvent> void sendSync(E event) {
        publisher.publishEvent(event);
    }

    @Async
    public <E extends CommanderEvent> void sendAsync(E event) {
        publisher.publishEvent(event);
    }
}
