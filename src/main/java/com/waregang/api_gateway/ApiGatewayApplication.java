package com.waregang.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({CorsProperties.class, GatewayProperties.class})
@SpringBootApplication
public class ApiGatewayApplication {

	static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
