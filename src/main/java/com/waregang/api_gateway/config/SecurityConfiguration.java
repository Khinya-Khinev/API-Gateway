package com.waregang.api_gateway.config;

import com.waregang.api_gateway.properties.AuthProperties;
import com.waregang.api_gateway.properties.CorsProperties;
import com.waregang.api_gateway.properties.GatewayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final GatewayProperties gatewayProperties;
    private final AuthProperties authProperties;
    private final CorsProperties corsProperties;

    @Autowired
    public SecurityConfiguration(
            ClientRegistrationRepository clientRegistrationRepository,
            GatewayProperties gatewayProperties,
            AuthProperties authProperties,
            CorsProperties corsProperties
    ) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.gatewayProperties = gatewayProperties;
        this.authProperties = authProperties;
        this.corsProperties = corsProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        RequestMatcher apiMatcher = PathPatternRequestMatcher
                .withDefaults()
                .matcher("/api/**");

        http
                .csrf(csrf -> csrf.disable()) // для Swagger / dev
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login/**",
                                "/oauth2/**",
                                "/actuator/health",
                                "/actuator/health/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers(EndpointRequest.to(HealthEndpoint.class))
                        .permitAll()

                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                apiMatcher
                        )
                        // всё остальное → редирект на OAuth2 login
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint(
                                        "/oauth2/authorization/" + gatewayProperties.clientId()
                                ),
                                new NegatedRequestMatcher(apiMatcher)
                        )
                )

                .oauth2Login(oauth2 -> {
                    var successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
                    successHandler.setDefaultTargetUrl(authProperties.successLoginRedirectUrl());
                    successHandler.setAlwaysUseDefaultTargetUrl(true);

                    oauth2.successHandler(successHandler);
                })

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                );

        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler handler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);
        handler.setPostLogoutRedirectUri(authProperties.postLogoutRedirectUrl());

        return handler;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
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