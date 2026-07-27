package com.eyyupsehmec.ortakkuran.service;

import com.eyyupsehmec.ortakkuran.config.MonitorProperties;
import com.eyyupsehmec.ortakkuran.model.MonitorResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorService {

    private final PlaywrightService playwrightService;
    private final EmailService emailService;
    private final MonitorProperties properties;
    private volatile int remainingPages;
    private static final ZoneId CST = ZoneId.of("America/Chicago");
    private int skipBucket;;
    private int threshold;

    /**
     * Prevent duplicate emails while the count remains below the threshold.
     */
    private volatile boolean alreadyNotified = false;

    /**
     * Number of scheduled monitor executions to skip.
     */
    private volatile int runsToSkip = 0;

    public void monitor() {
        LocalTime now = LocalTime.now(CST);

        if (now.isBefore(LocalTime.of(6, 0)) ||
                now.isAfter(LocalTime.of(23, 50))) {
            log.info("Skipping monitor. Outside monitoring window (06:00 - 23:50 CST).");
            return;
        }

        if (runsToSkip > 0) {
            log.info(
                    "Skipping scheduled monitor. {} scheduled run(s) remaining.",
                    runsToSkip);

            runsToSkip--;
            return;
        }

        log.info("========================================");
        log.info("Starting monitor");
        log.info("Threshold: {}", threshold);

        try {
            threshold = properties.getThreshold();
            skipBucket = properties.getSkipBucket();
            MonitorResult result = playwrightService.check(properties.getUrl());
            log.info("Remaining pages: {}", result.pageCount());

            runsToSkip = Math.max(0, (result.pageCount() - 1) / skipBucket);
            remainingPages = result.pageCount();
            log.info("skip-bucket: {}", skipBucket);
            if (runsToSkip > 0) {
                log.info(
                        "Remaining pages: {}. Skipping the next {} scheduled run(s).",
                        result.pageCount(),
                        runsToSkip);
            } else {
                log.info("Remaining pages below {}. Monitoring will run again in next {}, minutes", skipBucket, getNextInterval());
            }

            result.pages().forEach(page ->
                    log.info(" - {}", page));

            if (result.pageCount() < threshold) {

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

    public Duration getNextInterval() {
        MonitorProperties.FastCheck fastCheck = properties.getFastCheck();

        if (remainingPages > fastCheck.getMinPages()
                && remainingPages <= fastCheck.getMaxPages()) {
            return fastCheck.getInterval();
        }

        return properties.getInterval();
    }

}