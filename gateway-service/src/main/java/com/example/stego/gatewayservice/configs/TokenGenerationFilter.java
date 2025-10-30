package com.example.stego.gatewayservice.configs;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.stream.Collectors;

@Configuration
public class TokenGenerationFilter {

    private final JwtEncoder jwtEncoder;

    public TokenGenerationFilter(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @Bean
    public GlobalFilter customGlobalFilter() {
        return (exchange, chain) -> exchange
                .getPrincipal().flatMap(principal -> {
                    if (principal instanceof OAuth2AuthenticationToken) {

                        var authentication = (Authentication) principal;
                        var oauth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
                        var issuer = "http://gateway-service";
                        var now = Instant.now();
                        long expiry = 3600L;

                        var scope = authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.joining(" "));
                        var claims = JwtClaimsSet.builder()
                                .issuer(issuer)
                                .issuedAt(now)
                                .expiresAt(now.plusSeconds(expiry))
                                .subject(oauth2User.getName())
                                .claim("scope", scope)
                                .claim("name", oauth2User.getAttributes().get("name"))
                                .build();
                        var token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

                        exchange.getRequest().mutate().header("Authorization", "Bearer " + token).build();
                    }
                    return chain.filter(exchange);
                });
    }

}
