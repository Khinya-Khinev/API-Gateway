package com.waregang.api_gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.gateway")
public record GatewayProperties (
        String clientId,
        String clientSecret
) {}






