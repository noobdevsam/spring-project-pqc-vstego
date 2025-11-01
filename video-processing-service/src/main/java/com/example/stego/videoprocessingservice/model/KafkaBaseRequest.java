package com.example.stego.videoprocessingservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaBaseRequest {

    private String jobId;
    private String inputFileGridFsId;

}
