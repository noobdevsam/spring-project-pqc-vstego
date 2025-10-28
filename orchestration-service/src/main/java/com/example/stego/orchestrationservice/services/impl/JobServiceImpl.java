package com.example.stego.orchestrationservice.services.impl;

import com.example.stego.orchestrationservice.document.Job;
import com.example.stego.orchestrationservice.model.KafkaJobCompletion;
import com.example.stego.orchestrationservice.services.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    @Override
    public Map<String, String> createEncodeJob(OAuth2User principal, MultipartFile carrierFile, MultipartFile secretFile, String recipientUserId, String senderPrivateKey) {
        return Map.of();
    }

    @Override
    public Map<String, String> createDecodeJob(OAuth2User principal, MultipartFile stegoFile, String recipientPrivateKey) {
        return Map.of();
    }

    @Override
    public Job getJobStatus(String jobId, OAuth2User principal) {
        return null;
    }

    @Override
    public Map<String, Object> estimateCapacity(MultipartFile carrierFile) {
        return Map.of();
    }

    @Override
    public ResponseEntity<Resource> getDownloadableFile(String jobId, OAuth2User principal) {
        return null;
    }

    @Override
    public void handleJobCompletion(KafkaJobCompletion completion) {

    }
}
