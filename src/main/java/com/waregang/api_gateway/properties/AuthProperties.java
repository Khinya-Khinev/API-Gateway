package com.waregang.api_gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties (
        String successLoginRedirectUrl,
        String postLogoutRedirectUrl//,
        //String logoutUrl
){}
