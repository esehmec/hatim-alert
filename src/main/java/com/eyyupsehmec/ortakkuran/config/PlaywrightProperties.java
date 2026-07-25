package com.eyyupsehmec.ortakkuran.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "playwright")
public class PlaywrightProperties {

    private boolean headless = true;

}