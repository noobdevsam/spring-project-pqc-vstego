package com.example.stego.orchestrationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KafkaDecodeRequest extends KafkaBaseRequest {

    private String recipientPrivateKey; // For decryption

    // Constructor
    public KafkaDecodeRequest(String jobId, String inputFile, String privateKey) {
        super(jobId, inputFile);
        this.recipientPrivateKey = privateKey;
    }

}
