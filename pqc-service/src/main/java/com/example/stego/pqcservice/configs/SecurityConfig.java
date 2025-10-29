package com.example.stego.pqcservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        // Allow public access to generate keys and view public keys
                        .requestMatchers(HttpMethod.POST, "/api/v1/keys/generate").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}/keys").permitAll()
                        // Require authentication to set a key
                        .requestMatchers("/api/v1/keys/set").authenticated()
                        // Allow actuator health checks
                        .requestMatchers("/actuator/**").permitAll()
                        // Deny all other requests
                        .anyRequest().denyAll()
                )
                // Configure as a stateless resource server
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

}
