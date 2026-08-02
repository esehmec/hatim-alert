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

    /**
     * Number of reminder emails sent while in the fast-check zone.
     */
    private volatile int fastCheckReminderCount = 0;

    /**
     * Prevent duplicate threshold notifications.
     */
    private volatile boolean thresholdNotificationSent = false;

    public void monitor() {
        LocalTime now = LocalTime.now(CST);
        int maxReminders = properties.getFastCheck().getMaxReminders();
        if (now.isBefore(LocalTime.of(1, 0)) ||
                now.isAfter(LocalTime.of(1, 1))) {
            log.info("Skipping monitor. Outside monitoring window ");
            return;
        }

        log.info("========================================");
        log.info("Starting monitor");

        try {
            int threshold = properties.getThreshold();

            MonitorProperties.FastCheck fastCheck = properties.getFastCheck();

            MonitorResult result = playwrightService.check(properties.getUrl());

            remainingPages = result.pageCount();

            log.info("Remaining pages: {}", remainingPages);

            log.info("Next monitor scheduled in {}.", getNextInterval());

            result.pages()
                    .stream()
                    .findFirst()
                    .ifPresent(page -> log.info("Next page: - {}", page));
            handleNotifications(result, fastCheck, threshold, maxReminders);
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
            MonitorProperties.FastCheck fastCheck,
            int threshold,
            int maxReminders) {

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