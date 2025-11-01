package com.example.stego.videoprocessingservice.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KafkaDecodeRequest extends KafkaBaseRequest {
    private String recipientPrivateKey; // for decryption

    @Builder
    public KafkaDecodeRequest(
            String jobId,
            String inputFileGridFsId,
            String recipientPrivateKey
    ) {
        super(jobId, inputFileGridFsId);
        this.recipientPrivateKey = recipientPrivateKey;
    }
}
