package com.example.stego.videoprocessingservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PublicKeyDTO {

    private String userId;
    private String kemPublicKey; // CRYSTALS-Kyber public key
    private String dsaPublicKey; // CRYSTALS-Dilithium public key
    private boolean isActive;

}
