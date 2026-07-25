FROM mcr.microsoft.com/playwright/java:v1.54.0-jammy

WORKDIR /app

COPY target/ortak-kuran-monitor-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]