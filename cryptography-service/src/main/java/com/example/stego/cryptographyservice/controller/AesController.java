package com.example.stego.cryptographyservice.controller;

import com.example.stego.cryptographyservice.sevices.CryptographyService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
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

//    /**
//     * Encrypts a file stream using AES-256 GCM.
//     * Returns the encrypted stream with IV prepended.
//     */
//    @PostMapping("/encrypt")
//    public ResponseEntity<InputStreamResource> encryptFile(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("aesKey") String base64AesKey) {
//        try {
//            // Decode the AES key
//            var keyBytes = Base64.getDecoder().decode(base64AesKey);
//            var aesKey = new SecretKeySpec(keyBytes, "AES");
//
//            // Get input stream from uploaded file
//            var dataStream = file.getInputStream();
//
//            // Encrypt the stream
//            var encryptedStream = cryptographyService.encryptData(dataStream, aesKey);
//
//            // Prepare response
//            InputStreamResource resource = new InputStreamResource(encryptedStream);
//
//            var headers = new HttpHeaders();
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=encrypted_" + file.getOriginalFilename());
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(resource);
//
//        } catch (IOException | IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    /**
//     * Decrypts a file stream using AES-256 GCM.
//     * Expects the input stream to have IV prepended to ciphertext.
//     */
//    @PostMapping("/decrypt")
//    public ResponseEntity<InputStreamResource> decryptFile(
//            @RequestParam("file") MultipartFile encryptedFile,
//            @RequestParam("aesKey") String base64AesKey) {
//
//        try {
//            // Decode the AES key
//            var keyBytes = Base64.getDecoder().decode(base64AesKey);
//            var aesKey = new SecretKeySpec(keyBytes, "AES");
//
//            // Get input stream from uploaded file
//            var encryptedStream = encryptedFile.getInputStream();
//
//            // Decrypt the stream
//            var decryptedStream = cryptographyService.decryptData(encryptedStream, aesKey);
//
//            // Prepare response
//            var resource = new InputStreamResource(decryptedStream);
//
//            var headers = new HttpHeaders();
//            var originalName = encryptedFile.getOriginalFilename();
//            var decryptedName = originalName != null && originalName.startsWith("encrypted_")
//                    ? originalName.substring(10)
//                    : "decrypted_" + originalName;
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + decryptedName);
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(resource);
//
//        } catch (IOException | IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    /**
     * Service-to-service endpoint for encrypting input streams directly.
     * Used by other microservices for internal operations.
     */
    @PostMapping(value = "/encrypt-stream", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> encryptStream(
            InputStream dataStream,
            @RequestHeader("X-AES-Key") String base64AesKey) {

        try {
            // Decode the AES key
            var keyBytes = Base64.getDecoder().decode(base64AesKey);
            var aesKey = new SecretKeySpec(keyBytes, "AES");

            // Encrypt the stream
            var encryptedStream = cryptographyService.encryptData(dataStream, aesKey);

            // Return encrypted stream
            var resource = new InputStreamResource(encryptedStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Decrypts a file stream using AES-256 GCM.
     * Expects the input stream to have IV prepended to ciphertext.
     */
    @PostMapping("/decrypt")
    public ResponseEntity<InputStreamResource> decryptFile(
            @RequestParam("file") MultipartFile encryptedFile,
            @RequestParam("aesKey") String base64AesKey) {

        try {
            // Decode the AES key
            var keyBytes = Base64.getDecoder().decode(base64AesKey);
            var aesKey = new SecretKeySpec(keyBytes, "AES");

            // Get input stream from uploaded file
            var encryptedStream = encryptedFile.getInputStream();

            // Decrypt the stream
            var decryptedStream = cryptographyService.decryptData(encryptedStream, aesKey);

            // Prepare response
            var resource = new InputStreamResource(decryptedStream);

            var headers = new HttpHeaders();
            var originalName = encryptedFile.getOriginalFilename();
            var decryptedName = originalName != null && originalName.startsWith("encrypted_")
                    ? originalName.substring(10)
                    : "decrypted_" + originalName;
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + decryptedName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
