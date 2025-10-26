package com.example.stego.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        // Secure all API endpoints under /api/v1/users
                        .requestMatchers("/api/v1/users/**").authenticated()

                        // Allow Actuator health checks
                        .requestMatchers("/actuator/**").permitAll()

                        // Deny access to all other endpoints
                        .anyRequest().denyAll()
                )
                .build();
    }
}
