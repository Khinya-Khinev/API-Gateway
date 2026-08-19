package com.waregang.api_gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("receiving-service", r -> r
                        .path("/api/receiving-service/**")
                        .filters(GatewayFilterSpec::tokenRelay)
                        .uri("http://receiving-service:8080")
                )
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(GatewayFilterSpec::tokenRelay)
                        .uri("http://auth-service:9000")
                )
                .build();
    }
}