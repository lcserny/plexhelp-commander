package net.cserny.monitoring;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MonitoringLogAppenderRegistrar implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${logging.monitoring.name:net.cserny}")
    private String monitoringName;

    private final MonitoringRepository monitoringRepository;

    public MonitoringLogAppenderRegistrar(MonitoringRepository monitoringRepository) {
        this.monitoringRepository = monitoringRepository;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();

        MonitoringLogAppender appender = new MonitoringLogAppender();
        appender.setContext(ctx);
        appender.setMonitoringRepository(monitoringRepository);
        appender.start();

        ctx.getLogger(monitoringName).addAppender(appender);
    }
}
