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
    int maxReminders;

    /**
     * Number of reminder emails sent while in the fast-check zone.
     */
    private volatile int fastCheckReminderCount = 0;

    /**
     * Prevent duplicate threshold notifications.
     */
    private volatile boolean thresholdNotificationSent = false;

    /**
     * Number of scheduled monitor executions to skip.
     */
    private volatile int runsToSkip = 0;

    public void monitor() {
        LocalTime now = LocalTime.now(CST);
        maxReminders = properties.getFastCheck().getMaxReminders();
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

        try {
            threshold = properties.getThreshold();
            skipBucket = properties.getSkipBucket();

            MonitorProperties.FastCheck fastCheck = properties.getFastCheck();

            MonitorResult result = playwrightService.check(properties.getUrl());

            remainingPages = result.pageCount();

            log.info("Remaining pages: {}", remainingPages);

            runsToSkip = Math.max(0, (remainingPages - 1) / skipBucket);

            if (runsToSkip > 0) {
                log.info(
                        "Remaining pages: {}. Skipping the next {} scheduled run(s).",
                        remainingPages,
                        runsToSkip);
            } else {
                log.info(
                        "Remaining pages below {}. Monitoring will run again in {}.",
                        skipBucket,
                        getNextInterval());
            }

            result.pages()
                    .stream()
                    .findFirst()
                    .ifPresent(page -> log.info("Next page: - {}", page));
            handleNotifications(result, fastCheck);
            if (!result.pages().isEmpty()) {
                log.info("Last page: {}", result.pages().get(result.pages().size() - 1));
            }

        } catch (Exception ex) {
            log.error("Monitoring failed.", ex);
        }

        log.info("Monitor finished.");
        log.info("========================================");
    }

    private void handleNotifications(
            MonitorResult result,
            MonitorProperties.FastCheck fastCheck) {

        int pageCount = result.pageCount();

        //
        // Threshold reached.
        //
        if (pageCount < threshold) {

            if (!thresholdNotificationSent) {
                log.warn("Threshold reached. Sending notification.");

                emailService.sendThresholdAlert(result);
                fastCheckReminderCount = 0;
                thresholdNotificationSent = true;
            } else {
                log.info("Threshold notification already sent.");
            }

            return;
        }

        //
        // Fast-check zone.
        //
        if (pageCount <= fastCheck.getMaxPages()
                && pageCount > fastCheck.getMinPages()) {
            thresholdNotificationSent = false;
            if (fastCheckReminderCount < maxReminders) {

                log.info(
                        "Fast-check reminder {}/{}.",
                        fastCheckReminderCount + 1,
                        maxReminders);

                emailService.sendWarning(result);
                fastCheckReminderCount++;
            } else {
                log.info("Maximum fast-check reminders already sent.");
            }

            return;
        }

        //
        // Normal monitoring.
        //
        thresholdNotificationSent = false;
        fastCheckReminderCount = 0;

        log.info("Everything looks good.");
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