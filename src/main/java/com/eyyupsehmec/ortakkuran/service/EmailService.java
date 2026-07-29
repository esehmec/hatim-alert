package com.eyyupsehmec.ortakkuran.service;

import com.eyyupsehmec.ortakkuran.config.NotificationProperties;
import com.eyyupsehmec.ortakkuran.model.MonitorResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationProperties notificationProperties;

    public void sendWarning(MonitorResult result) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(notificationProperties.getTo());
        message.setSubject(
                String.format("⚠️ Ortak Hatim Warning (%d pages remaining)", result.pageCount()));

        StringBuilder body = new StringBuilder();

        body.append("The remaining pages have entered the fast-check zone.\n\n");

        body.append("Remaining Pages: ")
                .append(result.pageCount())
                .append("\n");

        body.append("Checked At: ")
                .append(result.checkedAt())
                .append("\n\n");

        body.append("The monitor will continue checking at the fast-check interval until the remaining pages either return to the normal range or fall below the configured threshold.");

        message.setText(body.toString());

        log.info("Sending warning email to {}", notificationProperties.getTo());

        mailSender.send(message);

        log.info("Warning email sent successfully.");
    }

    public void sendThresholdAlert(MonitorResult result) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(notificationProperties.getTo());
        message.setSubject(
                String.format("🚨 ACTION REQUIRED - %d pages remaining", result.pageCount()));

        StringBuilder body = new StringBuilder();

        body.append("The configured page threshold has been reached.\n\n");

        body.append("Remaining Pages: ")
                .append(result.pageCount())
                .append("\n");

        body.append("Checked At: ")
                .append(result.checkedAt())
                .append("\n\n");

        body.append("Available Pages:\n");

        result.pages().forEach(page ->
                body.append("• ").append(page).append("\n"));

        body.append("\nPlease claim pages as soon as possible.");

        message.setText(body.toString());

        log.info("Sending threshold alert email to {}", notificationProperties.getTo());

        mailSender.send(message);

        log.info("Threshold alert email sent successfully.");
    }
}