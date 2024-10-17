package com.schnarbiesnmeowers.nmsgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class NmsGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(NmsGatewayApplication.class, args);
	}

	@Bean
	public PasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/*@Bean
	public RouteLocator myRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(p -> p
						.path("/actiontype/**")
						.uri("http://localhost:8080")) // Replace with your downstream service URL
				.build();
	}*/
}
