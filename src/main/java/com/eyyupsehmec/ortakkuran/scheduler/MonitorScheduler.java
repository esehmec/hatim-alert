package com.eyyupsehmec.ortakkuran.scheduler;

import com.eyyupsehmec.ortakkuran.config.MonitorProperties;
import com.eyyupsehmec.ortakkuran.service.MonitorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorScheduler {

    private final TaskScheduler taskScheduler;
    private final MonitorService monitorService;
    private final MonitorProperties properties;

    @PostConstruct
    public void schedule() {

        log.info(
                "Scheduling monitor every {}",
                properties.getInterval());

        taskScheduler.scheduleAtFixedRate(
                monitorService::monitor,
                Instant.now(),
                properties.getInterval());

    }

}