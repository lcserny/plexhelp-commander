package net.cserny.monitoring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Setter;

import java.time.Instant;

public class MonitoringLogAppender extends AppenderBase<ILoggingEvent> {

    @Setter
    private MonitoringRepository monitoringRepository;

    @Override
    protected void append(ILoggingEvent event) {
        if (monitoringRepository == null) {
            return;
        }

        if (event == null || event.getFormattedMessage() == null) {
            return;
        }

        // TODO tmp
        if (event.getLevel() != Level.DEBUG) {
            return;
        }

        monitoringRepository.save(MonitoredData.builder()
                        .timestamp(Instant.now())
                        .message(event.getFormattedMessage())
                        .build());
    }
}
