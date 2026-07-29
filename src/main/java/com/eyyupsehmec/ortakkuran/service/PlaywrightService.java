package com.eyyupsehmec.ortakkuran.service;

import com.eyyupsehmec.ortakkuran.config.PlaywrightProperties;
import com.eyyupsehmec.ortakkuran.model.MonitorResult;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaywrightService {

    private static final Pattern PAGE_PATTERN =
            Pattern.compile("\\d+\\.\\s*Sayfa");

    private final Playwright playwright;
    private final PlaywrightProperties properties;

    public MonitorResult check(String url) {

        log.info("Opening {}", url);

        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(properties.isHeadless())
        );

        try (browser) {

            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setViewportSize(1920, 3000)
            );

            Page page = context.newPage();

            page.navigate(url);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(5000);

            saveDebugFiles(page);

            List<String> pages = extractRemainingPages(page.locator("body").innerText());

            log.info("Found {} remaining pages", pages.size());

            return new MonitorResult(
                    pages.size(),
                    pages,
                    LocalDateTime.now()
            );
        }
    }

    private List<String> extractRemainingPages(String body) {

        int start = body.indexOf("SIRADAKİLER");
        int end = body.indexOf("TAMAMLANANLAR");

        if (start == -1 || end == -1 || end <= start) {
            return List.of(); // or whatever represents "no result"
        }

        String remainingSection = body.substring(start, end);

        List<String> pages = new ArrayList<>();

        Matcher matcher = PAGE_PATTERN.matcher(remainingSection);

        while (matcher.find()) {
            pages.add(matcher.group());
        }

        return pages;
    }

    private void saveDebugFiles(Page page) {

        try {
            Files.writeString(Paths.get("page.html"), page.content());

            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("page.png"))
                    .setFullPage(true));

        } catch (IOException e) {
            log.warn("Unable to save debug files.", e);
        }
    }
}