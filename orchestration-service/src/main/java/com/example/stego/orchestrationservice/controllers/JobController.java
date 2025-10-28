package com.example.stego.orchestrationservice.controllers;

import com.example.stego.orchestrationservice.document.Job;
import com.example.stego.orchestrationservice.services.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping("/api/v1/estimate")
    public ResponseEntity<Map<String, Object>> estimateCapacity(
            @RequestParam("file") MultipartFile carrierFile) {
        // Note: This endpoint is unauthenticated as it's just an estimation
        return ResponseEntity.ok(jobService.estimateCapacity(carrierFile));
    }

    @PostMapping("/api/v1/encode")
    public ResponseEntity<Map<String, String>> encode(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("carrierFile") MultipartFile carrierFile,
            @RequestParam("secretFile") MultipartFile secretFile,
            @RequestParam("recipientUserId") String recipientUserId,
            @RequestParam("senderPrivateKey") String senderPrivateKey) {

        Map<String, String> response = jobService.createEncodeJob(
                principal, carrierFile, secretFile, recipientUserId, senderPrivateKey
        );
        return ResponseEntity.accepted().body(response); // HTTP 202
    }

    @PostMapping("/api/v1/decode")
    public ResponseEntity<Map<String, String>> decode(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam("stegoFile") MultipartFile stegoFile,
            @RequestParam("recipientPrivateKey") String recipientPrivateKey) {

        Map<String, String> response = jobService.createDecodeJob(
                principal, stegoFile, recipientPrivateKey
        );
        return ResponseEntity.accepted().body(response); // HTTP 202
    }

    @GetMapping("/api/v1/job/{jobId}/status")
    public ResponseEntity<Job> getJobStatus(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String jobId) {

        return ResponseEntity.ok(jobService.getJobStatus(jobId, principal));
    }

    @GetMapping("/api/v1/job/{jobId}/download")
    public ResponseEntity<Resource> downloadFile(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String jobId) {

        return jobService.getDownloadableFile(jobId, principal);
    }
}