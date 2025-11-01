package com.example.stego.videoprocessingservice.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KafkaEncodeRequest extends KafkaBaseRequest {
    private String secretFileGridFsId;
    private String recipientUserId;
    private String senderPrivateKey; // for signing

    @Builder
    public KafkaEncodeRequest(
            String jobId,
            String inputFileGridFsId,
            String secretFileGridFsId,
            String recipientUserId,
            String senderPrivateKey
    ) {
        super(jobId, inputFileGridFsId);
        this.secretFileGridFsId = secretFileGridFsId;
        this.recipientUserId = recipientUserId;
        this.senderPrivateKey = senderPrivateKey;
    }
}
