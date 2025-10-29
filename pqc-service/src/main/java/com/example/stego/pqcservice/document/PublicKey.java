package com.example.stego.pqcservice.document;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document("public_keys")
public class PublicKey {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String kemPublicKey; // CRYSTALS-Kyber public key, Base64 encoded
    private String dsaPublicKey; // CRYSTALS-Dilithium public key, Base64 encoded

    private boolean isActive;

    @CreatedDate
    private Instant createdAt;

    @Builder
    public PublicKey(String userId, String kemPublicKey, String dsaPublicKey) {
        this.userId = userId;
        this.kemPublicKey = kemPublicKey;
        this.dsaPublicKey = dsaPublicKey;
        this.isActive = true;
    }
}

