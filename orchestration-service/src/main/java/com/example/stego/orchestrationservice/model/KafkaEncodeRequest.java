package com.example.stego.orchestrationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KafkaEncodeRequest extends KafkaBaseRequest {
    private String secretFileGridFsId;
    private String recipientUserId; // To fetch recipient's public key
    private String senderPrivateKey; // For signing

    // Constructor
    public KafkaEncodeRequest(String jobId, String inputFile, String secretFile, String recipientId, String senderKey) {
        super(jobId, inputFile);
        this.secretFileGridFsId = secretFile;
        this.recipientUserId = recipientId;
        this.senderPrivateKey = senderKey;
    }
}
