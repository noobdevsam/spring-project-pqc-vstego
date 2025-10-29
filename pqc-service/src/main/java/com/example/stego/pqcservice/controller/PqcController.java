package com.example.stego.pqcservice.controller;

import com.example.stego.pqcservice.document.PublicKey;
import com.example.stego.pqcservice.model.KeyPairDTO;
import com.example.stego.pqcservice.model.PublicKeyDTO;
import com.example.stego.pqcservice.sevices.PqcService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PqcController {

    private final PqcService pqcService;

    /**
     * Generate PQC key pair
     */
    @PostMapping("/keys/generate")
    public ResponseEntity<KeyPairDTO> generateKeys() {
        try {
            return ResponseEntity.ok(pqcService.generateKeys());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate keys", e);
        }
    }

    /**
     * Set active public key for the authenticated user
     */
    @PostMapping("/keys/set")
    public ResponseEntity<PublicKey> setPublicKey(
            @AuthenticationPrincipal Jwt principal,
            @RequestBody PublicKeyDTO publicKeyDto) {
        var userId = principal.getSubject(); // The 'sub' claim from the JWT
        var savedKey = pqcService.setPublicKey(userId, publicKeyDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedKey);
    }

    /**
     * Fetch active public keys for a specific user
     */
    @GetMapping("/users/{userId}/keys")
    public ResponseEntity<PublicKey> getPublicKeyForUser(@PathVariable String userId) {
        return pqcService.getPublicKeyForUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
