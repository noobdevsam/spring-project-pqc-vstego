package com.example.stego.cryptographyservice.controller;

import com.example.stego.cryptographyservice.sevices.CryptographyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/crypto/aes")
@RequiredArgsConstructor
public class AesController {

    private CryptographyService cryptographyService;

    /**
     * Generates a new AES-256 secret key.
     * Returns the key as Base64-encoded string for transmission.
     */
    @PostMapping("/generate-key")
    public ResponseEntity<Map<String, String>> generateAesKeyForEncryption() {
        try {
            var aesKey = cryptographyService.generateAESKey();
            var encodedKey = Base64.getEncoder().encodeToString(aesKey.getEncoded());

            var response = new HashMap<String, String>();
            response.put("aesKey", encodedKey);
            response.put("algorithm", "AES");
            response.put("keySize", "256");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            var error = new HashMap<String, String>();
            error.put("error", "Failed to generate AES key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

}
