package com.example.stego.gatewayservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // This configures the security rules as per SRS v1.3
        return http
                .authorizeExchange(exchanges -> exchanges
                        // 1. Allow all static resources for the React frontend
                        .pathMatchers(
                                "/", "/index.html", "/static/**", "/favicon.ico", "/manifest.json"
                        ).permitAll() // Public endpoints

                        // 2. Allow the OAuth2 login and callback URLs (SRS Requirement)
                        .pathMatchers(
                                "/oauth2/authorization/github",
                                "/login/oauth2/code/github"
                        ).permitAll()

                        // 3. (Optional) Allow logout
                        .pathMatchers("/logout").permitAll()

                        // 4. (Optional) Allow actuator health checks
                        .pathMatchers("/actuator/health").permitAll()

                        // 5. Secure ALL other routes (SRS Requirement)
                        .anyExchange().authenticated()
                )
                .oauth2Login(Customizer.withDefaults()) // Enable GitHub OAuth2 login flow
                .logout(
                        logout -> logout
                                .logoutUrl("/logout")
                        // TODO: Configure post-logout redirection if needed
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF for simplicity in this example
                .build();
    }
}
