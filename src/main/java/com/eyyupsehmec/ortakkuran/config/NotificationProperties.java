package com.eyyupsehmec.ortakkuran.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    @Email
    @NotBlank
    private String to;

}