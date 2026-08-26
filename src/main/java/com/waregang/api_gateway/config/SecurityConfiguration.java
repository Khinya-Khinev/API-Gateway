package com.waregang.api_gateway.config;

import com.waregang.api_gateway.properties.AuthProperties;
import com.waregang.api_gateway.properties.CorsProperties;
import com.waregang.api_gateway.properties.GatewayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    private final GatewayProperties gatewayProperties;
    private final AuthProperties authProperties;
    private final CorsProperties corsProperties;

    @Autowired
    public SecurityConfiguration(ReactiveClientRegistrationRepository clientRegistrationRepository, GatewayProperties gatewayProperties, AuthProperties authProperties, CorsProperties corsProperties) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.gatewayProperties = gatewayProperties;
        this.authProperties = authProperties;
        this.corsProperties = corsProperties;
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
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // for Swagger, dev environment
//                .csrf(csrf -> csrf
//                        .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
//                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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

                .oauth2Login(oauth2 -> {
                    // Создаем хандлер
                    var successHandler = new RedirectServerAuthenticationSuccessHandler(
                            authProperties.successLoginRedirectUrl()
                    );
                    successHandler.setRequestCache(NoOpServerRequestCache.getInstance());
                    oauth2.authenticationSuccessHandler(successHandler);
                })

                .requestCache(cache -> cache.requestCache(NoOpServerRequestCache.getInstance()))

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                );

        return http.build();
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler() {
        var handler = new OidcClientInitiatedServerLogoutSuccessHandler(this.clientRegistrationRepository);
        handler.setPostLogoutRedirectUri(authProperties.postLogoutRedirectUrl());

        return handler;
    }

    @Bean
    public WebFilter csrfCookieWebFilter() {
        return (exchange, chain) -> {
            Mono<CsrfToken> csrfToken = exchange.getAttributeOrDefault(
                    CsrfToken.class.getName(), Mono.empty()
            );
            return csrfToken
                    .doOnSuccess(token -> {})
                    .then(chain.filter(exchange));
        };
    }

    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(corsProperties.allowedOrigins());
        corsConfig.setAllowedMethods(corsProperties.allowedMethods());
        corsConfig.setAllowedHeaders(corsProperties.allowedHeaders());
        corsConfig.setAllowCredentials(corsProperties.allowCredentials());
        corsConfig.setMaxAge(corsProperties.maxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}