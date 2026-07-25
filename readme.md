# Hatim Alert

A lightweight Spring Boot application that monitors the Ortak Kuran website and sends an email notification when the number of remaining pages drops below a configurable threshold.

The application uses **Playwright** to scrape the website and can run locally or inside Docker.

---

## Features

- 📖 Monitors remaining Quran pages
- 📧 Sends email notifications
- ⏰ Configurable monitoring interval
- 🕕 Configurable monitoring window
- 🐳 Docker support
- 📝 Detailed logging
- 🚫 Prevents duplicate email notifications while the page count remains below the threshold

---

## Tech Stack

- Java 17
- Spring Boot 3
- Playwright for Java
- Docker
- Gmail SMTP

---

## Configuration

Configure the application in `application.yml`.

Example:

```yaml
monitor:
  url: https://example.com
  threshold: 600
  interval: PT1H
  start-time: "06:00"
  end-time: "23:50"

notification:
  to: your@email.com

spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

---

## Environment Variables

Do **not** store your email credentials in Git.

Instead, export them as environment variables.

macOS/Linux:

```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-gmail-app-password
```

Verify they are set:

```bash
echo $MAIL_USERNAME
echo $MAIL_PASSWORD
```

---

## Running Locally

Build the application:

```bash
mvn clean install
```

Run:

```bash
java -jar target/ortak-kuran-monitor-1.0.0.jar
```

---

## Running with Docker

Build the project:

```bash
mvn clean install
```

Build the Docker image:

```bash
docker compose build --no-cache
```

Start:

```bash
docker compose up -d
```

View logs:

```bash
docker compose logs -f
```

Stop:

```bash
docker compose down
```

---

## Monitoring Schedule

The scheduler runs every configured interval (default: every hour).

Monitoring only occurs between:

- Start Time (default: 06:00 America/Chicago)
- End Time (default: 23:50 America/Chicago)

Outside that window, the application skips monitoring.

---

## Email Notifications

An email is sent only when:

- Remaining pages are **below the configured threshold**, and
- A notification has not already been sent for the current low-page condition.

Once the remaining page count rises back above the threshold, notifications are reset and can be sent again.

---

## Project Structure

```
src
├── config
│   ├── MonitorProperties
│   ├── NotificationProperties
│   └── PlaywrightConfiguration
├── scheduler
│   └── MonitorScheduler
├── service
│   ├── EmailService
│   ├── MonitorService
│   └── PlaywrightService
└── model
    └── MonitorResult
```

---

## Future Improvements

- Discord notifications
- Slack notifications
- SMS support
- Health endpoint
- Metrics with Prometheus
- Deploy to Railway, AWS, or Cloudflare
- Multiple monitored websites
- Web dashboard
- Persistent notification history

---

## License

MIT