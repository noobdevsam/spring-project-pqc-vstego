package com.example.stego.gatewayservice.controller;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwkSetRestController {

    private final JWKSet jwkSet;

    public JwkSetRestController(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
    }

    // Add endpoint methods here to expose the JWK Set as needed
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwkSet() {
        return jwkSet.toJSONObject();
    }

}
