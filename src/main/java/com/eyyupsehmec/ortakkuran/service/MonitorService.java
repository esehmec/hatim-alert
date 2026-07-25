package com.eyyupsehmec.ortakkuran.service;

import com.eyyupsehmec.ortakkuran.config.MonitorProperties;
import com.eyyupsehmec.ortakkuran.model.MonitorResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorService {

    private final PlaywrightService playwrightService;
    private final EmailService emailService;
    private final MonitorProperties properties;

    private static final ZoneId CST = ZoneId.of("America/Chicago");

    /**
     * Prevent duplicate emails while the count remains below the threshold.
     */
    private volatile boolean alreadyNotified = false;

    public void monitor() {
        LocalTime now = LocalTime.now(CST);

        if (now.isBefore(LocalTime.of(6, 0)) ||
                now.isAfter(LocalTime.of(23, 50))) {
            log.info("Skipping monitor. Outside monitoring window (06:00 - 23:50 CST).");
            return;
        }

        log.info("========================================");
        log.info("Starting monitor");
        log.info("Threshold: {}", properties.getThreshold());

        try {

            MonitorResult result =
                    playwrightService.check(properties.getUrl());

            log.info("Remaining pages: {}", result.pageCount());

            result.pages().forEach(page ->
                    log.info(" - {}", page));

            if (result.pageCount() < properties.getThreshold()) {

                if (!alreadyNotified) {

                    log.warn(
                            "Threshold reached. Sending notification.");

                    emailService.send(result);

                    alreadyNotified = true;

                } else {

                    log.info("Notification already sent.");

                }

            } else {

                alreadyNotified = false;

                log.info("Everything looks good.");

            }

        } catch (Exception ex) {

            log.error("Monitoring failed.", ex);

        }

        log.info("Monitor finished.");
        log.info("========================================");

    }

}