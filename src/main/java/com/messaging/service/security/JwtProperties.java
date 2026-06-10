package com.messaging.service.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("app.jwt")
@Getter @Setter
public class JwtProperties {
    private String secret;
    private long ttlMinutes;
    private String issuer;
}
