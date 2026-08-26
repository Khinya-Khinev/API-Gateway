package com.waregang.api_gateway;

import com.waregang.api_gateway.properties.AuthProperties;
import com.waregang.api_gateway.properties.CorsProperties;
import com.waregang.api_gateway.properties.GatewayProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({
		CorsProperties.class,
		GatewayProperties.class,
		AuthProperties.class
})
@SpringBootApplication
public class ApiGatewayApplication {
	static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
}
