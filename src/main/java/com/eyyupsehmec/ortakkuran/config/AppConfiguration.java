package com.eyyupsehmec.ortakkuran.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        MonitorProperties.class,

        NotificationProperties.class,
        PlaywrightProperties.class
})
public class AppConfiguration {

    @Bean(destroyMethod = "close")
    public Playwright playwright() {
        return Playwright.create();
    }

//    @Bean(destroyMethod = "close")
//    public Browser browser(
//            Playwright playwright,
//            PlaywrightProperties properties) {
//
//        return playwright.chromium().launch(
//                new BrowserType.LaunchOptions()
//                        .setHeadless(properties.isHeadless())
//        );
//    }

}