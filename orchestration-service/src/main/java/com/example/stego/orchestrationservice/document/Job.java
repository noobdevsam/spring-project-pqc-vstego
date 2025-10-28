package com.example.stego.orchestrationservice.document;

import com.example.stego.orchestrationservice.model.StorageDetails;
import com.example.stego.orchestrationservice.model.enums.JobStatus;
import com.example.stego.orchestrationservice.model.enums.JobType;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
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

}
