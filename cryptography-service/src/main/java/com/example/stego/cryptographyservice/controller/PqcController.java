package com.example.stego.cryptographyservice.controller;

import com.example.stego.cryptographyservice.document.PublicKey;
import com.example.stego.cryptographyservice.model.KeyPairDTO;
import com.example.stego.cryptographyservice.model.PublicKeyDTO;
import com.example.stego.cryptographyservice.sevices.CryptographyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/keys")
@RequiredArgsConstructor
public class PqcController {

    private final CryptographyService cryptographyService;

    /**
     * Generate PQC key pair
     */
    @PostMapping("/generate")
    public ResponseEntity<KeyPairDTO> generateKeys() {
        try {
            return ResponseEntity.ok(cryptographyService.generatePQCKeys());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate keys", e);
        }
    }

    /**
     * Set active public key for the authenticated user
     */
    @PostMapping("/set")
    public ResponseEntity<PublicKey> setPublicKey(
            @AuthenticationPrincipal Jwt principal,
            @RequestBody PublicKeyDTO publicKeyDto) {
        var userId = principal.getSubject(); // The 'sub' claim from the JWT
        var savedKey = cryptographyService.setPublicKey(userId, publicKeyDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedKey);
    }

    /**
     * Fetch active public keys for a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<PublicKey> getPublicKeyForUser(@PathVariable String userId) {
        return cryptographyService.getPublicKeyForUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
