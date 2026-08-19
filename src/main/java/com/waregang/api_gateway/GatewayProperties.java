package com.waregang.api_gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("app.gateway")
public record GatewayProperties (
        String clientId,
        String clientSecret
) {}






