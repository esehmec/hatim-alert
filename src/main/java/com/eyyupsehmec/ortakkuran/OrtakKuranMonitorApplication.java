package com.eyyupsehmec.ortakkuran;

import org.springframework.scheduling.annotation.EnableScheduling;
import com.eyyupsehmec.ortakkuran.config.MonitorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(MonitorProperties.class)
public class OrtakKuranMonitorApplication {

    public static void main(String[] args) {

        SpringApplication.run(OrtakKuranMonitorApplication.class, args);

    }

}