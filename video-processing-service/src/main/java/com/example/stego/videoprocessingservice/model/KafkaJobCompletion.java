package com.example.stego.videoprocessingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaJobCompletion {

    private String jobId;
    private JobStatus status;
    private String outputFileGridFsId;
    private String errorMessage;

}
