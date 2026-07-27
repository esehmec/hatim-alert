package com.eyyupsehmec.ortakkuran.scheduler;

import com.eyyupsehmec.ortakkuran.config.MonitorProperties;
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
    private final MonitorProperties properties;

    /**
     * Kick off monitoring when the application starts.
     *
     * We run the first monitor immediately instead of waiting for the first
     * scheduled interval so the application begins reporting right away.
     */
    @PostConstruct
    public void initialize() {
        // Perform the initial check immediately.
        monitorService.monitor();
        // Schedule the next run using the interval determined from the latest results.
        scheduleMonitor(resolveNextInterval());
    }

    /**
     * Schedule a single future execution.
     *
     * We intentionally schedule one run at a time instead of using a fixed-rate
     * scheduler because the delay may change after every execution (for example,
     * switching to a 30-minute interval when the remaining pages are low).
     */
    private void scheduleMonitor() {
        taskScheduler.schedule(() -> {
            try {
                // Execute the monitoring logic.
                monitorService.monitor();
            } finally {
                // Always schedule the next execution, even if monitoring throws an exception.
                // This keeps the scheduler alive and allows the next interval to adapt based
                // on the latest application state.
                scheduleMonitor(resolveNextInterval());
            }
        }, Instant.now().plus(resolveNextInterval()));
    }

    private void scheduleMonitor(Duration interval) {
        log.info("Scheduling next monitor in {}", interval);

        taskScheduler.schedule(() -> {
            try {
                // Execute the monitoring logic.
                monitorService.monitor();
            } finally {
                // Always schedule the next execution, even if monitoring throws an exception.
                // This keeps the scheduler alive and allows the next interval to adapt based
                // on the latest application state.
                scheduleMonitor(resolveNextInterval());
            }
        }, Instant.now().plus(interval));
    }

    /**
     * Ask the monitor service how long to wait before the next execution.
     *
     * The service can return different intervals depending on the remaining page
     * count (for example, 15 minutes when the remaining pages are between 10 and
     * 49, otherwise the configured default interval).
     */
    private Duration resolveNextInterval() {
        return monitorService.getNextInterval();
    }
}