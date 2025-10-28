package com.example.stego.orchestrationservice.document;

import com.example.stego.orchestrationservice.model.StorageDetails;
import com.example.stego.orchestrationservice.model.enums.JobStatus;
import com.example.stego.orchestrationservice.model.enums.JobType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Objects;

@Document(collection = "jobs")
public class Job {

    @Id
    private String id;

    @Indexed(unique = true)
    private String jobId; // UUID

    private JobType jobType;
    private JobStatus jobStatus;
    private String statusMessage;

    @Indexed
    private String senderUserId; // The user who initiated the job
    private String recipientUserId; // The user who is the target of the job, for Encoding jobs

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant completedAt;

    private StorageDetails storage = new StorageDetails();
    private String errorMessage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(String recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public StorageDetails getStorage() {
        return storage;
    }

    public void setStorage(StorageDetails storage) {
        this.storage = storage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Job job)) return false;

        return Objects.equals(id, job.id) && Objects.equals(jobId, job.jobId) && jobType == job.jobType && jobStatus == job.jobStatus && Objects.equals(statusMessage, job.statusMessage) && Objects.equals(senderUserId, job.senderUserId) && Objects.equals(recipientUserId, job.recipientUserId) && Objects.equals(createdAt, job.createdAt) && Objects.equals(completedAt, job.completedAt) && Objects.equals(storage, job.storage) && Objects.equals(errorMessage, job.errorMessage);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(jobId);
        result = 31 * result + Objects.hashCode(jobType);
        result = 31 * result + Objects.hashCode(jobStatus);
        result = 31 * result + Objects.hashCode(statusMessage);
        result = 31 * result + Objects.hashCode(senderUserId);
        result = 31 * result + Objects.hashCode(recipientUserId);
        result = 31 * result + Objects.hashCode(createdAt);
        result = 31 * result + Objects.hashCode(completedAt);
        result = 31 * result + Objects.hashCode(storage);
        result = 31 * result + Objects.hashCode(errorMessage);
        return result;
    }

}
