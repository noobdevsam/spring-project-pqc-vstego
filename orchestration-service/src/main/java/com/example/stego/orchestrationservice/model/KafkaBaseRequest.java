package com.example.stego.orchestrationservice.model;

import lombok.Data;

@Data
public class KafkaBaseRequest {
    private String jobId;
    private String inputFileGridFsId;
}
