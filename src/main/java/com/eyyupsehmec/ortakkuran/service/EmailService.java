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

    public void send(MonitorResult result) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(notificationProperties.getTo());

        message.setSubject("⚠ Ortak Hatim Alert");

        StringBuilder body = new StringBuilder();

        body.append("The remaining pages are below the configured threshold.\n\n");

        body.append("Remaining Pages: ")
                .append(result.pageCount())
                .append("\n\n");

        body.append("Pages:\n");

        result.pages().forEach(page ->
                body.append("• ").append(page).append("\n"));

        body.append("\n");

        body.append("Checked At: ")
                .append(result.checkedAt());

        message.setText(body.toString());

        log.info("Sending email to {}", notificationProperties.getTo());

        mailSender.send(message);

        log.info("Email sent successfully.");

    }

}