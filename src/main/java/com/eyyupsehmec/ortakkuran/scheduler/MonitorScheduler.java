package com.eyyupsehmec.ortakkuran.scheduler;

import com.eyyupsehmec.ortakkuran.service.MonitorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorScheduler {

    private final TaskScheduler taskScheduler;
    private final MonitorService monitorService;

    @PostConstruct
    public void initialize() {
        monitorService.monitor();
        scheduleMonitor(monitorService.getNextInterval());
    }

    private void scheduleMonitor(Duration interval) {
        log.info("Scheduling next monitor in {}", interval);

        taskScheduler.schedule(() -> {
            try {
                monitorService.monitor();
            } catch (Exception e) {
                log.error("Monitor execution failed.", e);
            } finally {
                scheduleMonitor(monitorService.getNextInterval());
            }
        }, Instant.now().plus(interval));
    }
}