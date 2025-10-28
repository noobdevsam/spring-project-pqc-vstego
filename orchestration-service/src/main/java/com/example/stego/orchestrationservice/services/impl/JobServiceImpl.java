package com.example.stego.orchestrationservice.services.impl;

import com.example.stego.orchestrationservice.document.Job;
import com.example.stego.orchestrationservice.model.KafkaDecodeRequest;
import com.example.stego.orchestrationservice.model.KafkaEncodeRequest;
import com.example.stego.orchestrationservice.model.KafkaJobCompletion;
import com.example.stego.orchestrationservice.model.enums.JobStatus;
import com.example.stego.orchestrationservice.model.enums.JobType;
import com.example.stego.orchestrationservice.repos.JobRepository;
import com.example.stego.orchestrationservice.services.JobService;
import com.example.stego.orchestrationservice.services.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

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
        var userId = getGithubId(principal);

        // 1. Upload files to GridFS via FileService
        var carrierFileId = uploadFile(carrierFile, userId);
        var secretFileId = uploadFile(secretFile, userId);

        // 2. Create and save Job entity (SRS FR-B-4.2)
        var job = new Job();
        job.setJobId(UUID.randomUUID().toString());
        job.setJobType(JobType.ENCODE);
        job.setJobStatus(JobStatus.PENDING);
        job.setSenderUserId(userId);
        job.setRecipientUserId(recipientUserId);
        job.getStorage().setInputFileGridFsId(carrierFileId);
        job.getStorage().setSecretFileGridFsId(secretFileId);
        jobRepository.save(job);

        // 3. Publish to Kafka
        var request = new KafkaEncodeRequest(
                job.getJobId(), carrierFileId, secretFileId, recipientUserId, senderPrivateKey
        );
        kafkaProducerService.sendEncodeRequest(request);

        return Map.of("jobId", job.getJobId());
    }

    @Override
    public Map<String, String> createDecodeJob(OAuth2User principal, MultipartFile stegoFile, String recipientPrivateKey) {
        var userId = getGithubId(principal);

        // 1. Upload stego-video to GridFS
        var stegoFileId = uploadFile(stegoFile, userId);

        // 2. Create and save Job entity
        var job = new Job();
        job.setJobId(UUID.randomUUID().toString());
        job.setJobType(JobType.DECODE);
        job.setJobStatus(JobStatus.PENDING);
        job.setSenderUserId(userId);
        job.getStorage().setInputFileGridFsId(stegoFileId);
        jobRepository.save(job);

        // 3. Publish to Kafka
        KafkaDecodeRequest request = new KafkaDecodeRequest(
                job.getJobId(), stegoFileId, recipientPrivateKey
        );
        kafkaProducerService.sendDecodeRequest(request);

        return Map.of("jobId", job.getJobId());
    }

    @Override
    public Job getJobStatus(String jobId, OAuth2User principal) {
        var userId = getGithubId(principal);
        return jobRepository.findByJobId(jobId)
                .filter(job -> job.getSenderUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found or access denied."));
    }

    @Override
    public Map<String, Object> estimateCapacity(MultipartFile carrierFile) {
        // This proxies the request to the video-processing-service
        // as it's the only service with ffprobe installed.
        try {
            var body = new LinkedMultiValueMap<String, Object>();
            body.add("file", carrierFile.getResource());

            return videoServiceRestClient.post()
                    .uri(VIDEO_SERVICE_ESTIMATE_URI)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("Capacity estimation failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Capacity estimation failed.");
        }
    }

    @Override
    public ResponseEntity<Resource> getDownloadableFile(String jobId, OAuth2User principal) {
        var userId = getGithubId(principal);
        var job = jobRepository.findByJobId(jobId)
                .filter(j -> j.getSenderUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found or access denied."));

        if (job.getJobStatus() != JobStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job is not yet complete.");
        }

        var fileId = job.getStorage().getOutputFileGridFsId();
        if (fileId == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Job completed but no output file ID was recorded.");
        }

        // Stream the file from file-service
        try {
            return fileServiceRestClient.get()
                    .uri(FILE_SERVICE_DOWNLOAD_URI, fileId)
                    .retrieve()
                    .toEntity(Resource.class);
        } catch (Exception e) {
            log.error("File download proxy failed for fileId: {}", fileId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve file from storage.");
        }
    }

    @Override
    @KafkaListener(
            topics = "${pqcstego.topics.job-completion}",
            groupId = "${orchestration-consumer-group}",
            containerFactory = "KafkaListenerContainerFactory"
    )
    public void handleJobCompletion(KafkaJobCompletion completion) {
        log.info("Received job completion status for jobId: {}", completion.getJobId());

        var job = jobRepository.findByJobId(completion.getJobId())
                .orElseThrow(() -> new RuntimeException("Received completion for unknown jobId: " + completion.getJobId()));

        job.setJobStatus(completion.getStatus());

        if (completion.getStatus() == JobStatus.COMPLETED) {
            job.getStorage().setOutputFileGridFsId(completion.getOutputFileGridFsId());
            job.setStatusMessage("Job completed successfully.");
        } else {
            job.setErrorMessage(completion.getErrorMessage());
            job.setStatusMessage("Job failed.");
        }

        job.setCompletedAt(Instant.now());
        jobRepository.save(job);
    }
}
