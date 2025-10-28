package com.example.stego.orchestrationservice.model;

import com.example.stego.orchestrationservice.model.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaJobCompletion {
    private String jobId;
    private JobStatus status; // Will be COMPLETED or FAILED
    private String outputFileGridFsId; // Null if failed
    private String errorMessage; // Null if completed
}
