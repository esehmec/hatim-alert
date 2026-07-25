package com.eyyupsehmec.ortakkuran.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "monitor")
public class MonitorProperties {

    @NotBlank
    private String url;

    private Duration interval = Duration.ofHours(1);

    @Min(1)
    private int threshold = 5;

}