package com.waregang.api_gateway.config;

import com.waregang.api_gateway.properties.GatewayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    private final GatewayProperties gatewayProperties;

    @Autowired
    public SecurityConfiguration(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }


    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        var apiEntryPoint = new DelegatingServerAuthenticationEntryPoint(
                new DelegatingServerAuthenticationEntryPoint.DelegateEntry(
                        ServerWebExchangeMatchers.pathMatchers("/api/**"),
                        (exchange, ex) -> {
                            exchange
                                    .getResponse()
                                    .setStatusCode(HttpStatus.UNAUTHORIZED);

                            return exchange
                                    .getResponse()
                                    .setComplete();
                        }
                )
        );

        apiEntryPoint.setDefaultEntryPoint(
                new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/" + gatewayProperties.clientId())
        );

        http
               // .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                )
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/login/**",
                                "/oauth2/**",
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**").permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(apiEntryPoint))
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout.logoutUrl("/api/v1/auth/logout"));

        return http.build();
    }

    @Bean
    public WebFilter csrfCookieWebFilter() {
        return (exchange, chain) -> {
            Mono<CsrfToken> csrfToken = exchange.getAttributeOrDefault(
                    CsrfToken.class.getName(), Mono.empty()
            );
            return csrfToken.doOnSuccess(token -> {
            }).then(chain.filter(exchange));
        };
    }
}