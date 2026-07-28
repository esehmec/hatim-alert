package com.eyyupsehmec.ortakkuran.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monitor")
public class MonitorProperties {

    //TODO: USE LOMBOK
    private String url;
    private int threshold;
    private Duration interval;
    private FastCheck fastCheck = new FastCheck();

    public void setSkipBucket(int skipBucket) {
        this.skipBucket = skipBucket;
    }

    private int skipBucket;


    public int getSkipBucket() {
        return skipBucket;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public Duration getInterval() {
        return interval;
    }

    public void setInterval(Duration interval) {
        this.interval = interval;
    }

    public FastCheck getFastCheck() {
        return fastCheck;
    }

    public void setFastCheck(FastCheck fastCheck) {
        this.fastCheck = fastCheck;
    }

    public static class FastCheck {

        private int minPages;
        private int maxPages;
        private Duration interval;
        private int maxReminders;

        public int getMinPages() {
            return minPages;
        }

        public void setMinPages(int minPages) {
            this.minPages = minPages;
        }

        public int getMaxPages() {
            return maxPages;
        }

        public void setMaxPages(int maxPages) {
            this.maxPages = maxPages;
        }

        public Duration getInterval() {
            return interval;
        }

        public void setInterval(Duration interval) {
            this.interval = interval;
        }

        public int getMaxReminders() {
            return maxReminders;
        }

        public void setMaxReminders(int maxReminders) {
            this.maxReminders = maxReminders;
        }
    }
}