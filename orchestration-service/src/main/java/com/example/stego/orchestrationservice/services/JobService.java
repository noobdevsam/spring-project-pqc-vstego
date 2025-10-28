package com.example.stego.orchestrationservice.services;

import com.example.stego.orchestrationservice.document.Job;
import com.example.stego.orchestrationservice.model.KafkaJobCompletion;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface JobService {

    Map<String, String> createEncodeJob(OAuth2User principal, MultipartFile carrierFile, MultipartFile secretFile, String recipientUserId, String senderPrivateKey);

    Map<String, String> createDecodeJob(OAuth2User principal, MultipartFile stegoFile, String recipientPrivateKey);

    Job getJobStatus(String jobId, OAuth2User principal);

    Map<String, Object> estimateCapacity(MultipartFile carrierFile);

    ResponseEntity<Resource> getDownloadableFile(String jobId, OAuth2User principal);

    void handleJobCompletion(KafkaJobCompletion completion);

}