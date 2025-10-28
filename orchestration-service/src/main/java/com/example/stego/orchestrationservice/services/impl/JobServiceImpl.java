package com.example.stego.orchestrationservice.services.impl;

import com.example.stego.orchestrationservice.document.Job;
import com.example.stego.orchestrationservice.model.KafkaJobCompletion;
import com.example.stego.orchestrationservice.repos.JobRepository;
import com.example.stego.orchestrationservice.services.JobService;
import com.example.stego.orchestrationservice.services.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final KafkaProducerService kafkaProducerService;
    private final RestClient fileServiceRestClient;
    private final RestClient videoServiceRestClient;

    private final String FILE_SERVICE_UPLOAD_URI = "/api/v1/internal/files/upload";
    private final String FILE_SERVICE_DOWNLOAD_URI = "/api/v1/internal/files/{fileId}";
    private final String VIDEO_SERVICE_ESTIMATE_URI = "/api/v1/internal/estimate";

    // Helper to get GitHub ID from principal
    private String getGithubId(OAuth2User principal) {
        return String.valueOf(principal.getAttributes().get("id"));
    }

    // Helper to call File Service and upload a file
    private String uploadFile(MultipartFile file, String userId) {
        try {
            var body = new LinkedMultiValueMap<String, Object>();
            body.add("file", file.getResource());
            body.add("userId", userId);
            var response = fileServiceRestClient.post()
                    .uri(FILE_SERVICE_UPLOAD_URI)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            assert response != null;
            return response.get("fileId").toString();
        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File service upload failed.");
        }
    }

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
